package org.teq.mearsurer.receiver;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;
import org.teq.visualizer.TimeChart;
import org.teq.visualizer.MetricsDisplayer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SendingMetricsReceiver<T extends BuiltInMetrics> extends AbstractReceiver implements Runnable{
    static private DockerRunner dockerRunner;

    //raw data will be processed first, then produce one data per second to TimeChart
    static private BlockingQueue<Double> rawOverallProcessingLatencyQueue;
    static private BlockingQueue<Double> rawOverallTransferLatencyQueue;
    static private List<BlockingQueue<Double>> rawProcessingLatencyQueueList;
    static private AtomicInteger processedQueryCount = new AtomicInteger(0);
    static private AtomicDouble energyConsumption = new AtomicDouble(0);
    private Class<T> typeClass;
    public SendingMetricsReceiver(MetricsDisplayer metricsDisplayer, Class<T> typeClass, DockerRunner dockerRunner) {
        super(metricsDisplayer);
        this.typeClass = typeClass;
        SendingMetricsReceiver.dockerRunner = dockerRunner;
    }

    @Override
    public void beginReceive(){
        Thread thread = new Thread(this);
        thread.start();
    }
    protected static final Logger logger = LogManager.getLogger(SendingMetricsReceiver.class);

    /**
     * finish the process (print or display the metrics)
     * @param set
     * @param <T>
     */
    protected static <T extends BuiltInMetrics> void finishStream(Set<T> set) {
        List<T> list = new ArrayList<>(set);
        T first = list.get(0);

        //these code is used to directly print the metrics
//        System.out.println("******************************************************");
//        System.out.println("Stream "+first.getId()+" finished");
//        for(int i=0; i<list.size(); i++) {
//            var metrics = list.get(i);
//            System.out.println("from" + DockerRuntimeData.getNodeNameById(metrics.getFromNodeId()) + " to " + DockerRuntimeData.getNodeNameById(metrics.getToNodeId())+ ":\n" +
//                    "   process time:" + (metrics.getTimestampOut() -  metrics.getTimestampIn())/1000/1000 + "ms\n" +
//                    "   package length:" + metrics.getPackageLength()/1024 + "KB" + "\n" +
//                    "   info type:" + metrics.getInfoType());
//
//            if(i == list.size()-1)break;
//            System.out.println(" transfer latency:" + (list.get(i+1).getTimestampIn() - metrics.getTimestampOut())/1000/1000 + "ms\n");
//            System.out.println("===============");
//        }

        //process the raw data
        processedQueryCount.getAndAdd(1);

        double overallProcessingLatency = (list.get(list.size()-1).getTimestampOut() - first.getTimestampIn())/1000.0/1000.0;
        double overallTransferLatency = 0;
        for(int i=0; i<list.size()-1; i++) {
            energyConsumption.getAndAdd(transferEnergyParam.get(list.get(i).getFromNodeId())*list.get(i).getPackageLength());
            energyConsumption.getAndAdd(transferEnergyParam.get(list.get(i).getToNodeId())*list.get(i).getPackageLength());
            overallTransferLatency += (list.get(i+1).getTimestampIn() - list.get(i).getTimestampOut())/1000.0/1000.0;
        }
        try {
            rawOverallProcessingLatencyQueue.put(overallProcessingLatency);
            rawOverallTransferLatencyQueue.put(overallTransferLatency);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //FIXME: use getLayerIdByNodeId will be better
        for (T t : list) {
            try {
                rawProcessingLatencyQueueList.get(DockerRuntimeData.getLayerIdByName(
                                DockerRuntimeData.getLayerNameByNodeName(
                                        DockerRuntimeData.getNodeNameById(t.getFromNodeId()))))
                        .put((t.getTimestampOut() - t.getTimestampIn()) / 1000.0 / 1000.0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * check if the stream is valid
     * @param set
     * @param <T>
     */
    private static <T extends BuiltInMetrics> void finishStreamTest(Set<T> set){
        List<T> list = new ArrayList<>(set);
        if(list.get(list.size()-1).getInfoType() != InfoType.Respond ||
                list.get(list.size()-1).getToNodeId() != -1) {
            return;
        }
        //check if all the metrics is valid(the toNode of the former one is the fromNode of the latter one)
        for(int i=0; i<list.size()-1; i++) {
            if(list.get(i).getToNodeId() != list.get(i+1).getFromNodeId()) {
                return;
            }
        }
        finishStream(set);
    }

    public static List<Double> nodeEnergyParam = new ArrayList<>();
    public static List<Double> transferEnergyParam = new ArrayList<>();
    private static <T extends BuiltInMetrics> void calUserDefinedMetrics(T metrics) {
        int nodeId = metrics.getFromNodeId();
        energyConsumption.getAndAdd(nodeEnergyParam.get(nodeId)*dockerRunner.cpuUsageArray.get(nodeId));
    }

    @Override
    public void run() {
        nodeEnergyParam = new ArrayList<>();
        transferEnergyParam = new ArrayList<>();
        for(int i=0; i<DockerRuntimeData.getNodeNameList().size(); i++) {
            nodeEnergyParam.add(1.0);
            transferEnergyParam.add(0.01);
        }
        // Initialize only the raw data queues needed for data processing
        rawOverallProcessingLatencyQueue = new LinkedBlockingQueue<>();
        rawOverallTransferLatencyQueue = new LinkedBlockingQueue<>();
        rawProcessingLatencyQueueList = new ArrayList<>();
        for(int i=0; i<DockerRuntimeData.getLayerList().size(); i++) {
            rawProcessingLatencyQueueList.add(new LinkedBlockingQueue<>());
        }
        // Create TimeChart instances - no need for time queues as they are managed automatically
        TimeChart<Double> overallProcessingLatencyChart = new TimeChart<>("overall processing latency/ms", "latency", "overall processing latency", "overview");
        TimeChart<Double> overallTransferLatencyChart = new TimeChart<>("overall transfer latency/ms", "latency", "overall transfer latency", "overview");
        TimeChart<Double> processingLatencyChart = new TimeChart<>("processing latency/ms", DockerRuntimeData.getLayerList(), "processing latency", "overview");
        TimeChart<Integer> processedQueryChart = new TimeChart<>("processed query", "count", "processed query", "user");
        TimeChart<Double> energyConsumptionChart = new TimeChart<>("energy consumption", "Energy Unit", "energy consumption", "overview");
        
        metricsDisplayer.addChart(overallProcessingLatencyChart);
        metricsDisplayer.addChart(overallTransferLatencyChart);
        metricsDisplayer.addChart(processingLatencyChart);
        metricsDisplayer.addChart(processedQueryChart);
        metricsDisplayer.addChart(energyConsumptionChart);
        //add a new thread to process the raw data and produce the data per second
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
//                       logger.info("a new second");
                        Thread.sleep(1000);
                        
                        // Add energy consumption data point
                        energyConsumptionChart.addDataPoint(energyConsumption.get());
                        energyConsumption.set(0);

                        // Add processed query data point
                        processedQueryChart.addDataPoint(processedQueryCount.get());
                        processedQueryCount.set(0);
                        
                        // Process overall processing latency
                        double overallProcessingLatency = 0.0;
                        int overallProcessingLatencyCount = 0;
                        while(!rawOverallProcessingLatencyQueue.isEmpty()) {
                            overallProcessingLatency += rawOverallProcessingLatencyQueue.poll();
                            overallProcessingLatencyCount++;
                        }
                        if(overallProcessingLatencyCount != 0) {
                            overallProcessingLatencyChart.addDataPoint(overallProcessingLatency / overallProcessingLatencyCount);
                        }
                        
                        // Process overall transfer latency
                        double overallTransferLatency = 0.0;
                        int overallTransferLatencyCount = 0;
                        while(!rawOverallTransferLatencyQueue.isEmpty()) {
                            overallTransferLatency += rawOverallTransferLatencyQueue.poll();
                            overallTransferLatencyCount++;
                        }
                        if(overallTransferLatencyCount != 0) {
                            overallTransferLatencyChart.addDataPoint(overallTransferLatency / overallTransferLatencyCount);
                        }
                        
                        // Process processing latency for each layer
                        {
                            boolean isEmpty = true;
                            for (int i = 0; i < rawProcessingLatencyQueueList.size(); i++) {
                                if (!rawProcessingLatencyQueueList.get(i).isEmpty()) {
                                    isEmpty = false;
                                    break;
                                }
                            }
                            if (!isEmpty) {
                                List<Double> layerLatencies = new ArrayList<>();
                                for (int i = 0; i < rawProcessingLatencyQueueList.size(); i++) {
                                    double processingLatency = 0;
                                    double processingLatencyCount = 0;
                                    while (!rawProcessingLatencyQueueList.get(i).isEmpty()) {
                                        processingLatency += rawProcessingLatencyQueueList.get(i).poll();
                                        processingLatencyCount++;
                                    }
                                    layerLatencies.add(processingLatencyCount > 0 ? processingLatency / processingLatencyCount : 0.0);
                                }
                                processingLatencyChart.addDataPoint(layerLatencies);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<T> stream = env.addSource(new CommonDataReceiver<T>( 8888,typeClass)).returns(TypeInformation.of(typeClass));
        stream.map(new MapFunction<T, T>() {
            private Map<UUID,TreeSet<T>> metricsMap = new HashMap<>();
            @Override
            public T map(T value) throws Exception {
                calUserDefinedMetrics(value);
                if(!metricsMap.containsKey(value.getId())) {
                    TreeSet<T> set = new TreeSet<>(Comparator.comparingLong(T::getTimestampIn));
                    set.add(value);
                    metricsMap.put(value.getId(),set);
                } else {
                    TreeSet<T> set = metricsMap.get(value.getId());
                    set.add(value);
                    if(set.last().getToNodeId() == -1) { //sink
                        finishStreamTest(set);
                        metricsMap.remove(value.getId());
                    }
                }
                return value;
            }
        }).setParallelism(1);
        try {
            env.setParallelism(1);
            env.execute();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
}
