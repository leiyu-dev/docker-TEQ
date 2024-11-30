package utils.datatest;

import org.teq.node.AbstractDockerNode;
import org.teq.node.DockerNodeParameters;
import org.teq.utils.DockerRuntimeData;

import java.util.Objects;

public class TestNode extends AbstractDockerNode {

    public TestNode() {
        super(new DockerNodeParameters());
    }

    @Override
    public void process() {
        var layerList = DockerRuntimeData.getLayerList();
        System.out.println("Layer List:");
        for(var layer : layerList) {
            System.out.println(layer);
        }
        System.out.println("===================================");

        var nodeList = DockerRuntimeData.getNodeNameList();
        System.out.println("Node List:");
        for(var node : nodeList) {
            System.out.println(node);
        }

        System.out.println("===================================");

        int id = getNodeID();
        String nodeName = getNodeName();
        if(id != DockerRuntimeData.getNodeIdByName(nodeName))
            System.out.println("Node ID and Node Name are not matching");
        else
            System.out.println("Node ID and Node Name are matching");
        if(!nodeName.equals(DockerRuntimeData.getNodeNameById(id))) {
            System.out.println("Node ID and Node Name are not matching");
            System.out.println("get Node name: " + DockerRuntimeData.getNodeNameById(id) + ", Node Name: " + nodeName);
        }
        else
            System.out.println("Node ID and Node Name are matching");

        System.out.println("===================================");

        var layer1List = DockerRuntimeData.getNodeNameListByLayerName("layer1");
        System.out.println("Node List in layer1:");
        for(var node : layer1List) {
            System.out.println(node);
        }
        System.out.println("===================================");

        var layer2List = DockerRuntimeData.getNodeNameListByLayerName("layer2");
        var node2 = layer2List.get(0);
        String layer2Name = DockerRuntimeData.getLayerNameByNodeName(node2);
        if(!Objects.equals(layer2Name, "layer2")) {
            System.out.println("Layer Name and Node Name are not matching");
            System.out.println(layer2Name);
        }
        else
            System.out.println("Layer Name and Node Name are matching");
    }
}
