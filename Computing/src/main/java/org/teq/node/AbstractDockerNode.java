package org.teq.node;

public abstract class AbstractDockerNode implements Node{
    public DockerNodeParameters parameters;
    public AbstractDockerNode(DockerNodeParameters parameters){
        this.parameters = parameters;
    }

    public void setParameters(DockerNodeParameters parameters){
        this.parameters = parameters;
    }

    /*
    * This method will be modified when asseemble the code in the docker. It will return the real node id
    * @return the **distinct** id of the node
    */  
    public static int getNodeID(){
        return Integer.parseInt(System.getenv("NODE_ID"));
    }

    public static String getNodeName(){
        return System.getenv("NODE_NAME");
    }

}
