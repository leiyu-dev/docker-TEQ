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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

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
        metrics.setCpuUsage(getCPUUsage());
        metrics.setPackageLength(packageLength);
        metrics.setFromNodeId(getNodeID());
        logger.debug("Begin metrics: " + metrics);
        metricsMap.put(dataId,metrics);
    }

    /**
     * this function must be called when a stream will be sent to other nodes
     * if you call this function for a stream, do not call endProcess()
     * @param dataId
     * @param toNodeId
     */
    static public void finishProcess(UUID dataId, int toNodeId){
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setTimestampOut(System.nanoTime());
        metrics.setToNodeId(toNodeId);
        logger.debug("Finish metrics: " + metrics);
        metricsMap.remove(dataId);
        queue.offer(metrics);
    }

    /**
     * this function must be called when a stream will never be sent to other nodes(which means it has come to its final sink)
     * if you call this function for a stream, do not call finishProcess()
     * @param dataId
     */
    static public void endProcess(UUID dataId){
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setTimestampOut(System.nanoTime());
        metrics.setToNodeId(-1);
        logger.debug("End metrics: " + metrics);
        metricsMap.remove(dataId);
        queue.offer(metrics);
    }
    public void flinkProcess(){
        initConnect();
        Thread monitorThread = new Thread(MeasuredFlinkNode::monitorCPUUsage);
        monitorThread.setDaemon(true); // 设置为守护线程
        monitorThread.start();
        try {
            dataProcess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final AtomicReference<Double> cpuUsageCache = new AtomicReference<>(0.0);
    private static volatile boolean running = true;

    public static double getCPUUsage() {
        return cpuUsageCache.get();
    }

    private static void monitorCPUUsage() {
        long[] previous = readCpuStats();

        while (running) {
            try {
                // 等待 1 秒
                Thread.sleep(1000);

                // 读取最新的 CPU 使用数据
                long[] current = readCpuStats();
                long totalDiff = current[0] - previous[0];
                long containerDiff = current[1] - previous[1];

                // 计算 CPU 使用率
                if (totalDiff > 0) {
                    double cpuUsage = (double) containerDiff / totalDiff * 100;
                    cpuUsageCache.set(cpuUsage); // 更新共享变量
                }

                // 更新历史值
                previous = current;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private static long[] readCpuStats() {
        long totalCpuTime = readTotalCpuTime();
        long containerCpuTime = readContainerCpuTime();
        return new long[]{totalCpuTime, containerCpuTime};
    }

    private static long readTotalCpuTime() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
            String line = reader.readLine();
            if (line.startsWith("cpu ")) {
                String[] tokens = line.split("\\s+");
                long user = Long.parseLong(tokens[1]);
                long nice = Long.parseLong(tokens[2]);
                long system = Long.parseLong(tokens[3]);
                long idle = Long.parseLong(tokens[4]);
                long iowait = Long.parseLong(tokens[5]);
                long irq = Long.parseLong(tokens[6]);
                long softirq = Long.parseLong(tokens[7]);
                return user + nice + system + idle + iowait + irq + softirq;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static long readContainerCpuTime() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/sys/fs/cgroup/cpu/cpuacct.usage"))) {
            String line = reader.readLine();
            return Long.parseLong(line) / 1000_000; // 转换为毫秒
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
