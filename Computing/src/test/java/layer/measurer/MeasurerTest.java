package layer.measurer;


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.layer.mearsurer.BuiltInMetrics;
import org.teq.simulator.Simulator;
import org.teq.utils.connector.CommonDataReceiver;

public class MeasurerTest {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator(new NetworkHostNode());
        for(int i=1;i<=5;i++){
            simulator.addNode(new MyFlinkNode());
        }
        simulator.start();
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver<BuiltInMetrics>(8888,BuiltInMetrics.class))
                .returns(TypeInformation.of(BuiltInMetrics.class))
                .map(BuiltInMetrics::toString);
        stream.print();
        env.execute();
    }
}