package utils.connector;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.mearsurer.MetricsPackageBean;
import org.teq.utils.connector.TargetedDataSender;

public class TargetedDataSenderTest {
    public static void main(String[] args) throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<String> stream = env.readTextFile("docker/file.txt");
        stream.map(new MapFunction<String, MetricsPackageBean>() {
            int count = 0;
            @Override
            public MetricsPackageBean map(String value) throws Exception {
                count++;
                System.out.println("ADD a Bean to port "+(1000+count));
                var bean = new MetricsPackageBean("localhost","localhost",1000+count,value);
                return bean;
            }
        }).addSink(new TargetedDataSender<MetricsPackageBean>(100, 1000));
        env.execute();
    }
}
