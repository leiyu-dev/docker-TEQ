package node.restricttest;

import node.commontest.MyFlinkNode;
import node.commontest.NetworkHostNode;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;

public class RestrictTest {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator(new NetworkHostNode());

        for(int i=1;i<=3;i++){
            DockerNodeParameters parameters = new DockerNodeParameters();
            parameters.setCpuUsageRate(0.1);
            simulator.addNode(new MyFlinkNode(parameters));
        }
        simulator.start();
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver<>( 8888,String.class)).returns(TypeInformation.of(String.class));
        stream.print();
        env.execute();
    }
}