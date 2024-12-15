package org.teq.mearsurer.receiver;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.Identifier;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.visualizer.MetricsDisplayer;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class SendingMetricsReceiver extends AbstractReceiver implements Runnable{
    public SendingMetricsReceiver(MetricsDisplayer metricsDisplayer) {
        super(metricsDisplayer);
    }

    @Override
    public void beginReceive(){
        Thread thread = new Thread(this);
        thread.start();
    }
    private static final Logger logger = LogManager.getLogger(SendingMetricsReceiver.class);
    static public void finishStream(Set<BuiltInMetrics> set){
        List<BuiltInMetrics> list = new ArrayList<>(set);
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
    static private Map<UUID,TreeSet<BuiltInMetrics>> metricsMap = new HashMap<>();
    @Override
    public void run() {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<BuiltInMetrics> stream = env.addSource(new CommonDataReceiver<BuiltInMetrics>( 8888,BuiltInMetrics.class)).returns(TypeInformation.of(BuiltInMetrics.class));
        stream.map(new MapFunction<BuiltInMetrics, BuiltInMetrics>() {
            @Override
            public BuiltInMetrics map(BuiltInMetrics value) throws Exception {
                logger.info("receive: "+value);
                if(!metricsMap.containsKey(value.getId())) {
                    TreeSet<BuiltInMetrics> set = new TreeSet<>(Comparator.comparingLong(BuiltInMetrics::getTimestampIn));
                    set.add(value);
                    metricsMap.put(value.getId(),set);
                } else {
                    TreeSet<BuiltInMetrics> set = metricsMap.get(value.getId());
                    set.add(value);
                    if(set.last().getToNodeId() == -1) { //sink
                        logger.info("stream "+value.getId()+" finished");
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
