package utils.datatest;

import node.commontest.MyFlinkNode;
import node.commontest.NetworkHostNode;
import org.teq.layer.Layer;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;

public class RuntimeDataTest {
    public static void main(String[] args) throws Exception {
        Layer layer1 = new Layer(new MyFlinkNode(),5,"layer1");
        Layer layer2 = new Layer(new MyFlinkNode(),5,"layer2");
        Simulator simulator = new Simulator(new NetworkHostNode());
        simulator.addNode(new TestNode());
        simulator.addLayer(layer1);
        simulator.addLayer(layer2);
        simulator.start();

    }
}
