package org.teq.layer.mearsurer;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.Simulator;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MetricsTransformer {
    private static final Logger logger = LogManager.getLogger(MetricsTransformer.class);
    private static final Map<UUID,Set<BuiltInMetrics>> metricsMap = new HashMap<>();
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

                @Override
                public BuiltInMetrics map(BuiltInMetrics value) throws Exception {
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
    public void beginTransform() throws Exception {
        Thread thread = new Thread(new MetricsReceiver());
        thread.start();
    }
}
