package layer;

import node.commontest.MyFlinkNode;
import node.commontest.NetworkHostNode;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.layer.Layer;
import org.teq.simulator.Simulator;
import org.teq.utils.connector.CommonDataReceiver;

public class LayerTest {
    public static void main(String[] args) throws Exception {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver<>( 8888,String.class)).returns(TypeInformation.of(String.class));
        stream.print();
        env.execute();
        Layer layer1 = new Layer(new MyFlinkNode(),5);
        Layer layer2 = new Layer(new MyFlinkNode(),5);
        Simulator simulator = new Simulator(new NetworkHostNode());
        simulator.addLayer(layer1);
        simulator.addLayer(layer2);
        simulator.start();
    }

}
