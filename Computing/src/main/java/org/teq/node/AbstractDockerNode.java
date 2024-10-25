package org.teq.node;

public abstract class AbstractDockerNode implements Node{
    /*
     * restrict the running environment of the node here
     */
    public double maxCpuUsage = 1.0;// 1.0 means 100%
    public double maxMemoryUsage = 1.0;// MB
}
