package org.teq.mearsurer;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.Simulator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.visualizer.Chart;
import org.teq.visualizer.MetricsDisplayer;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MetricsTransformer {
    private static final Logger logger = LogManager.getLogger(MetricsTransformer.class);
    private Simulator simulator;
    private final MetricsDisplayer metricsDisplayer;
    public MetricsTransformer(Simulator simulator, MetricsDisplayer metricsDisplayer) {
        this.simulator = simulator;
        this.metricsDisplayer = metricsDisplayer;
    }
    static void finishStream(Set<BuiltInMetrics>set){
        List<BuiltInMetrics>list = new ArrayList<>(set);
        BuiltInMetrics first = list.get(0);
        System.out.println("******************************************************");
        System.out.println("Stream "+first.getId()+" finished");
        for(int i=0; i<list.size(); i++) {
            var metrics = list.get(i);
            System.out.println("from" + DockerRuntimeData.getNodeNameById(metrics.getFromNodeId()) + " to " + DockerRuntimeData.getNodeNameById(metrics.getToNodeId())+ ":\n" +
                    "   process time:" + (metrics.getTimestampOut() -  metrics.getTimestampIn())/1000/1000 + "ms\n" +
                    "   cpu usage:" + (metrics.getCpuUsage()*100) + "%\n" +
                    "   memory usage:" + (metrics.getMemoryUsage()/1024/1024) + "MB\n" +
                    "   package length:" + metrics.getPackageLength()/1024 + "KB"  );

            if(i == list.size()-1)break;
            System.out.println(" transfer latency:" + (list.get(i+1).getTimestampIn() - metrics.getTimestampOut())/1000/1000 + "ms\n");
            System.out.println("===============");
        }
    }
    class MetricsReceiver implements Runnable{
        @Override
        public void run() {
            var env = StreamExecutionEnvironment.getExecutionEnvironment();
            DataStream<BuiltInMetrics> stream = env.addSource(new CommonDataReceiver<BuiltInMetrics>( 8888,BuiltInMetrics.class)).returns(TypeInformation.of(BuiltInMetrics.class));
            stream.map(new MapFunction<BuiltInMetrics, BuiltInMetrics>() {
                Map<UUID,Set<BuiltInMetrics>> metricsMap = new HashMap<>();
                @Override
                public BuiltInMetrics map(BuiltInMetrics value) throws Exception {
                    logger.info("receive: "+value);
                    if(!metricsMap.containsKey(value.getId())) {
                        Set<BuiltInMetrics> set = new TreeSet<>(Comparator.comparingLong(BuiltInMetrics::getTimestampIn));
                        set.add(value);
                        metricsMap.put(value.getId(),set);
                    } else {
                        Set<BuiltInMetrics> set = metricsMap.get(value.getId());
                        set.add(value);
                        if(value.getToNodeId() == -1) { //sink
                            finishStream(set);
                            metricsMap.remove(value.getId());
                        }
                    }
                    return value;
                }
            });
            try {
                env.setParallelism(1);
                env.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void beginMonitor() {
        List<String>nodeList = DockerRuntimeData.getNodeNameList();
        List<String>layerList = DockerRuntimeData.getLayerList();

        List<BlockingQueue<Double>> cpuUsageQueueList = new ArrayList<>();
        List<BlockingQueue<Double>> memoryUsageQueueList = new ArrayList<>();

        for (int i = 0; i < nodeList.size(); i++) {
            cpuUsageQueueList.add(new ArrayBlockingQueue<>(100));
            memoryUsageQueueList.add(new ArrayBlockingQueue<>(100));
        }

        List<BlockingQueue<Double>> cpuUsageLayerList = new ArrayList<>();//for each layer
        List<BlockingQueue<Double>> memoryUsageLayerList = new ArrayList<>();
        for (int i = 0; i < layerList.size(); i++) {
            cpuUsageLayerList.add(new ArrayBlockingQueue<>(100));
            memoryUsageLayerList.add(new ArrayBlockingQueue<>(100));
        }

        DockerRunner dockerRunner = simulator.getDockerRunner();
        dockerRunner.beginDockerMetricsCollection(cpuUsageQueueList, memoryUsageQueueList);

        for (int i = 0; i < layerList.size(); i++) {
            metricsDisplayer.addChart(new Chart(getTimeQueue(), cpuUsageLayerList.get(i), "time/s", "cpu usage/%", layerList.get(i) + " cpu usage"));
            metricsDisplayer.addChart(new Chart(getTimeQueue(), memoryUsageLayerList.get(i), "time/s", "memory usage/MB", layerList.get(i) + " memory usage"));
        }

        Thread thread = new Thread(() ->{
            while (true) {

                List<Integer>nodeCountList = new ArrayList<>();
                for (int i = 0; i < layerList.size(); i++) {
                    nodeCountList.add(0);
                }
                List<Double>cpuUsageLayerListSum = new ArrayList<>();
                List<Double>memoryUsageLayerListSum = new ArrayList<>();
                for (int i = 0; i < layerList.size(); i++) {
                    cpuUsageLayerListSum.add(0.0);
                    memoryUsageLayerListSum.add(0.0);
                }

                for (int i = 0; i < nodeList.size(); i++) {
                    try {
                        while(!cpuUsageQueueList.get(i).isEmpty()) {
//                        logger.info("try to take from queue");
                            double cpuUsage = (cpuUsageQueueList.get(i).take());
                            double memoryUsage = (memoryUsageQueueList.get(i).take());
//                        logger.info("take from queue");
                            String nodeName = nodeList.get(i);
                            String layerName = DockerRuntimeData.getLayerNameByNodeName(nodeName);
                            if (layerName == null) { // network node or other user defined node
                                continue;
                            }
                            int layerIndex = DockerRuntimeData.getLayerIdByName(layerName);
                            nodeCountList.set(layerIndex, nodeCountList.get(layerIndex) + 1);
                            cpuUsageLayerListSum.set(layerIndex, cpuUsageLayerListSum.get(layerIndex) + cpuUsage);
                            memoryUsageLayerListSum.set(layerIndex, memoryUsageLayerListSum.get(layerIndex) + memoryUsage);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (int i = 0; i < layerList.size(); i++) {
                    if (nodeCountList.get(i) == 0) {
                        logger.info("no node data in layer " + layerList.get(i));
                        continue;
                    }
                    try {
                        cpuUsageLayerList.get(i).put(cpuUsageLayerListSum.get(i) / nodeCountList.get(i));
                        memoryUsageLayerList.get(i).put(memoryUsageLayerListSum.get(i) / nodeCountList.get(i));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public BlockingQueue<Double> getTimeQueue() {
        BlockingQueue<Double> timeQueue = new ArrayBlockingQueue<>(100);
        Thread threadTime = new Thread(new Thread(){
            double time = 0;
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    time++;
                    try {
                        timeQueue.put(time);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        threadTime.start();
        return timeQueue;
    }

    public void beginTransform() throws Exception {
        //add a thread to add an element into timeQueue every second:
        beginMonitor();
        Thread threadReceiver = new Thread(new MetricsReceiver());
        threadReceiver.start();


    }
}
