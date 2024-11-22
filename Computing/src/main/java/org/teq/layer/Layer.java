package org.teq.layer;

import org.apache.commons.lang3.SerializationUtils;
import org.teq.node.AbstractDockerNode;
import org.teq.node.DockerNodeParameters;

import java.util.ArrayList;
import java.util.List;

/* a layer is a collection of nodes that shares the same process function
 * but different node properties(like CPU speed, memory, etc.)
 */
public class Layer {

    /* the function node that this layer is based on, other properties in functionNode will be the default parameter for all nodes
     */
    private static int layerCount = 0;
    private AbstractDockerNode functionNode;
    private String layerName;
    private List<DockerNodeParameters> paramList = new ArrayList<>();
    private int nodeCount;
    Layer(AbstractDockerNode functionNode,int nodeCount){
        this.functionNode = functionNode;
        this.layerName = "layer" + layerCount++;
        this.nodeCount = nodeCount;
        for(int i = 0; i < nodeCount; i++) {
            paramList.add(SerializationUtils.clone(functionNode.parameters));
        }
    }
    public void setLayerName(String layerName){
        this.layerName = layerName;
    }
    public void changeNodeParameter(int index,DockerNodeParameters parameters){
        paramList.set(index, parameters);
    }
    public AbstractDockerNode getFunctionNode(){
        return functionNode;
    }
    public DockerNodeParameters getNodeParameter(int index){
        return paramList.get(index);
    }

}
