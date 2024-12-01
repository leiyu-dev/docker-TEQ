package org.teq.layer.mearsurer;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DockerNodeParameters;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataSender;

import java.lang.management.ManagementFactory;
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
    void initConnect(){
        StreamExecutionEnvironment env = getEnv();
        DataStream<BuiltInMetrics> metricsDataStream = env.addSource(new SourceFunction<BuiltInMetrics>() {
            private volatile boolean isRunning = true;
            @Override
            public void run(SourceContext<BuiltInMetrics> ctx) throws Exception {
                while (isRunning) {
                    // 从队列中获取数据
                    BuiltInMetrics data = queue.poll();
                    if (data != null) {
                        ctx.collect(data);
                    }
                    Thread.sleep(100);
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        });
        metricsDataStream.addSink(new CommonDataSender<>(DockerRuntimeData.getNetworkHostNodeName(),
                SimulatorConfigurator.metricsPortBegin + getNodeID(), 100000, 1000));
    }

    static Map<UUID,BuiltInMetrics> metricsMap = new HashMap<>();
    // call this when finish every data process (usually means finish an object processing)
    static public void beginProcess(UUID dataId,int packageLength){
        logger.debug("Begin process data: " + dataId);
        BuiltInMetrics metrics = new BuiltInMetrics() ;
        metrics.setTimestampIn(System.nanoTime());
        metrics.setId(dataId);
        //FIXME: memory and cpu usage record is not accurate, need to find a better way to get memory usage
        metrics.setMemoryUsage(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
        metrics.setCpuUsage(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        metrics.setPackageLength(packageLength);
        metrics.setFromNodeId(getNodeID());
        logger.debug("Begin metrics: " + metrics);
        metricsMap.put(dataId,metrics);
    }
    static public void finishProcess(UUID dataId, long toNodeId){
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setTimestampOut(System.nanoTime());
        metrics.setToNodeId(toNodeId);
        logger.debug("Finish metrics: " + metrics);
        queue.offer(metrics);
    }
    public void flinkProcess(){
        initConnect();
        try {
            dataProcess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
