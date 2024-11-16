package org.teq.node;

public abstract class AbstractDockerNode implements Node{
    /*
     * restrict the running environment of the node here
     */
    public double maxCpuUsage = 1.0;// 1.0 means 100%
    public double maxMemoryUsage = 1.0;// MB


    /*
    * This method will be modified when asseemble the code in the docker. It will return the real node id instead of 0
    * @return the **distinct** id of the node
    */  
    public int getNodeID(){
        return 0;
    }
}
