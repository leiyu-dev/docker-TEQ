import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.SimulatorConfigurator;

public class FlinkTest {
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        conf.setInteger(RestOptions.PORT, 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
        String filePath = SimulatorConfigurator.hostPath + "/file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        process(input);
        input.print();
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    static void process(DataStream<String> a){
        a = a.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                Thread.sleep(1000000000);
                return s + "11";
            }
        });
    }
}
