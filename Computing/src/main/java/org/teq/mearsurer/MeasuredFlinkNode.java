package org.teq.mearsurer;

import com.alibaba.fastjson.JSON;
import com.github.dockerjava.core.DockerContextMetaFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.configurator.unserializable.InfoType;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DockerNodeParameters;
import org.teq.utils.DockerRuntimeData;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MeasuredFlinkNode extends AbstractFlinkNode {
    private static final Logger logger = LogManager.getLogger(MeasuredFlinkNode.class);
    abstract public void dataProcess() throws Exception;
    private static final BlockingQueue<BuiltInMetrics> queue = new LinkedBlockingQueue<>();
    public MeasuredFlinkNode(){
        super(new DockerNodeParameters());
    }
    public MeasuredFlinkNode(DockerNodeParameters parameters){
        super(parameters);
    }
    class MetricsSender implements Runnable{
        @Override
        public void run() {
            String serverHost = DockerRuntimeData.getNetworkHostNodeName();
            int serverPort = SimulatorConfigurator.metricsPort;

            Socket socket = null;
            OutputStream outputStream = null;

            while (true) {
                try {
                    if (socket == null || socket.isClosed()) {
                        logger.info("Attempting to connect to the server " + serverHost + ":" + serverPort);
                        socket = new Socket(serverHost, serverPort);
                        socket.setTrafficClass(0x10);
                        socket.setKeepAlive(true);
                        socket.setTcpNoDelay(true);
                        outputStream = socket.getOutputStream();
                        logger.info("Connected to the server " + serverHost + ":" + serverPort);
                    }
                    if(queue.isEmpty()){
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    if(outputStream == null){
                        logger.error("Failed to connect to the server " + serverHost + ":" + serverPort);
                        throw new IOException("Failed to connect to the server " + serverHost + ":" + serverPort);
                    }
                    String data = JSON.toJSONString(queue.take());
//                    logger.info("Sending Metrics: " + data);
                    outputStream.write((data+"\n").getBytes());
                    outputStream.flush();

                } catch (IOException e) {
                    logger.error("Failed to connect or send data. Retrying...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    if (socket != null && socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException ignored) {
                        }
                        socket = null;
                    }
                }
            }
        }
    }
    static Map<UUID,BuiltInMetrics> metricsMap = new HashMap<>();
    // call this when finish every data process (usually means finish an object processing)

    //TODO: decouple the sleep and metrics collection
    static public void beginProcess(UUID dataId, long timestampOut, String srcNodeName, MetricsPackageBean metricsPackageBean){
        long timestampIn = System.nanoTime();
        if(ExecutorParameters.useFixedLatency && srcNodeName != null && !srcNodeName.isEmpty()){
            long shouldSleep = DockerRuntimeData.getNodeParametersByNodeName(srcNodeName).getFixedOutLatency()
                    + DockerRuntimeData.getNodeParametersByNodeName(getNodeName()).getFixedInLatency()
                    - (timestampIn - timestampOut)/1000000;
//            logger.info("Should sleep: " + shouldSleep);
            if(shouldSleep > 0){
                try {
                    Thread.sleep(shouldSleep);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        logger.trace("Begin process data: " + dataId);
        BuiltInMetrics metrics = new BuiltInMetrics() ;
        metrics.setTimestampIn(System.nanoTime());
        metrics.setId(dataId);
        metrics.setFromNodeId(getNodeID());
        logger.trace("Begin metrics: " + metrics);
        metricsMap.put(dataId,metrics);
    }

    /**
     * this function must be called when a stream will be sent to other nodes
     * if you call this function for a stream, do not call endProcess()
     * @param dataId
     * @param toNodeId
     */
    static public void finishProcess(UUID dataId, int toNodeId, int packageLength, InfoType infoType, MetricsPackageBean metricsPackageBean){
        metricsPackageBean.setTimestampOut(System.nanoTime());
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setPackageLength(packageLength);
        metrics.setInfoType(infoType);
        metrics.setTimestampOut(System.nanoTime());
        metrics.setToNodeId(toNodeId);
        logger.trace("Finish metrics: " + metrics);
        metricsMap.remove(dataId);
        queue.offer(metrics);
    }

    /**
     * this function must be called when a stream will never be sent to other nodes(which means it has come to its final sink)
     * if you call this function for a stream, do not call finishProcess()
     * @param dataId
     */
    static public void endProcess(UUID dataId, int packageLength, InfoType infoType, MetricsPackageBean metricsPackageBean){
        metricsPackageBean.setTimestampOut(System.nanoTime());
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setTimestampOut(System.nanoTime());
        metrics.setPackageLength(packageLength);
        metrics.setInfoType(infoType);
        metrics.setToNodeId(-1);
        logger.trace("End metrics: " + metrics);
        metricsMap.remove(dataId);
        queue.offer(metrics);
    }
    public void flinkProcess(){
        Thread metricsSender = new Thread(new MetricsSender());
        metricsSender.setDaemon(true);
        metricsSender.start();
        try {
            dataProcess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
