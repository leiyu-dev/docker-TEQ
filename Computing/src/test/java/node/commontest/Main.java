package node.commontest;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.simulator.Simulator;
import org.teq.utils.connector.CommonDataReceiver;

public class Main {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator(new NetworkHostNode());
        for(int i=1;i<=5;i++){
            simulator.addNode(new MyFlinkNode());
        }
        simulator.start();
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver<String>(8888,String.class)).returns(TypeInformation.of(String.class));
        stream.print();
        env.execute();
    }
}