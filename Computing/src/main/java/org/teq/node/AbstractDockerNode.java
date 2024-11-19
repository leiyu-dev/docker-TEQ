package org.teq.node;

public abstract class AbstractDockerNode implements Node{
    /*
     * restrict the running environment of the node here
     */
    public double maxCpuUsage = 1.0;// 1.0 means 100%
    public double maxMemoryUsage = 1.0;// MB


    /*
    * This method will be modified when asseemble the code in the docker. It will return the real node id
    * @return the **distinct** id of the node
    */  
    public static int getNodeID(){
        return Integer.parseInt(System.getenv("NODE_ID"));
    }
}
