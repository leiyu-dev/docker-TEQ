package node.commontest;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.simulator.Simulator;
import org.teq.simulator.network.connector.CommonDataReceiver;

public class Main {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator(new NetworkHostNode());
        for(int i=1;i<=100;i++){
            simulator.addNode(new MyFlinkNode());
        }
        simulator.start();
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver(null, 8888));
        stream.print();
        env.execute();
    }
}