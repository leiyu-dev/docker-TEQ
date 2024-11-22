package org.teq.node;

import java.io.Serializable;

public class DockerNodeParameters implements Serializable {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    public enum CpuRestrictType{
        ROUGH, //roughly restrict the cpu speed,using the percentage of the cpu usage rate
        PRECISE //precisely restrict the cpu speed,using the exact cpu clock speed and cpu core number
    }
    /*
     * restrict the running environment of the node here
     */

    public CpuRestrictType cpuRestrictType = CpuRestrictType.ROUGH;
    /*precisely restrict the cpu speed,using the exact cpu clock speed and cpu core number*/
    public double cpuClockSpeed = 2.5;//GHz
    public int cpuCoreNumber = 1;//number of cores

    /*roughly restrict the cpu speed,using the percentage of the cpu usage rate*/
    public double cpuUsageRate = 0.5;//percentage of the cpu usage rate, 0.5 means 50%, it can be larger than 1(means using more than 1 core)

    /*restrict the memory size of the node*/
    public double memorySize = 2;//GB

    /*restrict the network bandwidth of the node*/
    public double networkInBandwidth = 100;//Kbps
    public double networkInLatency = 0;//ms
    public double networkOutBandwidth = 100;//Kbps
    public double networkOutLatency = 0;//ms

    @Override
    public String toString(){
        return "cpuRestrictType: " + cpuRestrictType + "\n" +
                "cpuClockSpeed: " + cpuClockSpeed + "\n" +
                "cpuCoreNumber: " + cpuCoreNumber + "\n" +
                "cpuUsageRate: " + cpuUsageRate + "\n" +
                "memorySize: " + memorySize + "\n" +
                "networkInBandwidth: " + networkInBandwidth + "\n" +
                "networkInLatency: " + networkInLatency + "\n" +
                "networkOutBandwidth: " + networkOutBandwidth + "\n" +
                "networkOutLatency: " + networkOutLatency + "\n";
    }
}
