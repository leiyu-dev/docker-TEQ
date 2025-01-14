package org.teq.mearsurer.receiver;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Identifier;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.visualizer.Chart;
import org.teq.visualizer.MetricsDisplayer;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SendingMetricsReceiver<T extends BuiltInMetrics> extends AbstractReceiver implements Runnable{

    static private BlockingQueue<Double> overallProcessingLatencyQueue;
    static private BlockingQueue<Double> overallTransferLatencyQueue;
    static private List<BlockingQueue<Double>> processingLatencyQueueList;

    //raw data will be processed first, then produce one data per second into the above queue
    static private BlockingQueue<Double> rawOverallProcessingLatencyQueue;
    static private BlockingQueue<Double> rawOverallTransferLatencyQueue;
    static private List<BlockingQueue<Double>> rawProcessingLatencyQueueList;
    private Class<T> typeClass;
    public SendingMetricsReceiver(MetricsDisplayer metricsDisplayer, Class<T> typeClass) {
        super(metricsDisplayer);
        this.typeClass = typeClass;
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
        double overallProcessingLatency = (list.get(list.size()-1).getTimestampOut() - first.getTimestampIn())/1000.0/1000.0;
        double overallTransferLatency = 0;
        for(int i=0; i<list.size()-1; i++) {
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
    @Override
    public void run() {
        overallProcessingLatencyQueue = new LinkedBlockingQueue<>();
        overallTransferLatencyQueue = new LinkedBlockingQueue<>();
        processingLatencyQueueList = new ArrayList<>();
        rawOverallProcessingLatencyQueue = new LinkedBlockingQueue<>();
        rawOverallTransferLatencyQueue = new LinkedBlockingQueue<>();
        rawProcessingLatencyQueueList = new ArrayList<>();
        for(int i=0; i<DockerRuntimeData.getLayerList().size(); i++) {
            processingLatencyQueueList.add(new LinkedBlockingQueue<>());
            rawProcessingLatencyQueueList.add(new LinkedBlockingQueue<>());
        }
        BlockingQueue<Double> overallProcessingLatencyTime = new LinkedBlockingQueue<>();
        BlockingQueue<Double> overallTransferLatencyTime = new LinkedBlockingQueue<>();
        BlockingQueue<Double> processingLatencyTime = new LinkedBlockingQueue<>();
        metricsDisplayer.addChart(new Chart( overallProcessingLatencyTime,overallProcessingLatencyQueue,
                "time/s","overall processing latency/ms","latency","overall processing latency"));
        metricsDisplayer.addChart(new Chart( overallTransferLatencyTime,overallTransferLatencyQueue,
                "time/s","overall transfer latency/ms","latency","overall transfer latency"));
        metricsDisplayer.addChart(new Chart( processingLatencyTime,processingLatencyQueueList,
                "time/s","processing latency/ms",DockerRuntimeData.getLayerList(),"processing latency"));

        //add a new thread to process the raw data and produce the data per second
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                while(true) {
                    try {
                        logger.info("a new second");
                        Thread.sleep(1000);
                        double overallProcessingLatency = 0.0;
                        int overallProcessingLatencyCount = 0;
                        double overallTransferLatency = 0.0;
                        int overallTransferLatencyCount = 0;
                        while(!rawOverallProcessingLatencyQueue.isEmpty()) {
                            overallProcessingLatency += rawOverallProcessingLatencyQueue.poll();
                            overallProcessingLatencyCount++;
                        }
                        while(!rawOverallTransferLatencyQueue.isEmpty()) {
                            overallTransferLatency += rawOverallTransferLatencyQueue.poll();
                            overallTransferLatencyCount++;
                        }
                        if(overallProcessingLatencyCount != 0) {
                            overallProcessingLatencyQueue.put(overallProcessingLatency / overallProcessingLatencyCount);
                            long time = System.currentTimeMillis() - startTime;
                            //两位小数
                            overallProcessingLatencyTime.put((double)Math.round(time/1000.0*10.0)/10.0);
                        }
                        if(overallTransferLatencyCount != 0) {
                            overallTransferLatencyQueue.put(overallTransferLatency / overallTransferLatencyCount);
                            long time = System.currentTimeMillis() - startTime;
                            overallTransferLatencyTime.put((double)Math.round(time/1000.0*10.0)/10.0);
                        }
                        boolean isEmpty = true;
                        for(int i=0; i<rawProcessingLatencyQueueList.size(); i++) {
                            if(!rawProcessingLatencyQueueList.get(i).isEmpty()){
                                isEmpty = false;
                                break;
                            }
                        }
                        if(isEmpty) {
                            continue;
                        }
                        for(int i=0; i<rawProcessingLatencyQueueList.size(); i++) {
                            double processingLatency = 0;
                            double processingLatencyCount = 0;
                            while(!rawProcessingLatencyQueueList.get(i).isEmpty()) {
                                processingLatency += rawProcessingLatencyQueueList.get(i).poll();
                                processingLatencyCount++;
                            }
                            processingLatencyQueueList.get(i).put  (processingLatency / processingLatencyCount);
                        }
                        long time = System.currentTimeMillis() - startTime;
                        processingLatencyTime.put((double)Math.round(time/1000.0*10)/10.0);
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
                logger.info("receive a new metrics:" + value);
                if(!metricsMap.containsKey(value.getId())) {
                    TreeSet<T> set = new TreeSet<>(Comparator.comparingLong(T::getTimestampIn));
                    set.add(value);
                    metricsMap.put(value.getId(),set);
                } else {
                    TreeSet<T> set = metricsMap.get(value.getId());
                    set.add(value);
                    if(set.last().getToNodeId() == -1) { //sink
                        SendingMetricsReceiver.logger.info("stream "+value.getId()+" finished");
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
