package org.teq.node;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class DockerNodeParameters implements Serializable {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public CpuRestrictType getCpuRestrictType() {
        return cpuRestrictType;
    }

    public void setCpuRestrictType(CpuRestrictType cpuRestrictType) {
        this.cpuRestrictType = cpuRestrictType;
    }

    public double getCpuClockSpeed() {
        return cpuClockSpeed;
    }

    public void setCpuClockSpeed(double cpuClockSpeed) {
        this.cpuClockSpeed = cpuClockSpeed;
    }

    public int getCpuCoreNumber() {
        return cpuCoreNumber;
    }

    public void setCpuCoreNumber(int cpuCoreNumber) {
        this.cpuCoreNumber = cpuCoreNumber;
    }

    public double getCpuUsageRate() {
        return cpuUsageRate;
    }

    public void setCpuUsageRate(double cpuUsageRate) {
        this.cpuUsageRate = cpuUsageRate;
    }

    public double getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(double memorySize) {
        this.memorySize = memorySize;
    }

    public double getNetworkOutBandwidth() {
        return networkOutBandwidth;
    }

    public void setNetworkOutBandwidth(double networkOutBandwidth) {
        this.networkOutBandwidth = networkOutBandwidth;
    }

    public double getNetworkOutLatency() {
        return networkOutLatency;
    }

    public void setNetworkOutLatency(double networkOutLatency) {
        this.networkOutLatency = networkOutLatency;
    }

    public double getNetworkInBandwidth() {
        return networkInBandwidth;
    }

    public void setNetworkInBandwidth(double networkInBandwidth) {
        this.networkInBandwidth = networkInBandwidth;
    }

    public double getNetworkInLatency() {
        return networkInLatency;
    }

    public void setNetworkInLatency(double networkInLatency) {
        this.networkInLatency = networkInLatency;
    }

    public int getFixedInLatency() {
        return fixedInLatency;
    }

    public void setFixedInLatency(int fixedInLatency) {
        this.fixedInLatency = fixedInLatency;
    }

    public int getFixedOutLatency() {
        return fixedOutLatency;
    }

    public void setFixedOutLatency(int fixedOutLatency) {
        this.fixedOutLatency = fixedOutLatency;
    }

    public double getNetworkOutPacketLossRate() {
        return networkOutPacketLossRate;
    }

    public void setNetworkOutPacketLossRate(double networkOutPacketLossRate) {
        this.networkOutPacketLossRate = networkOutPacketLossRate;
    }

    public enum CpuRestrictType{
        ROUGH, //roughly restrict the cpu speed,using the percentage of the cpu usage rate
        PRECISE //precisely restrict the cpu speed,using the exact cpu clock speed and cpu core number
    }
    /*
     * restrict the running environment of the node here
     * Not Implemented Yet
     */
    @JSONField(serialize = false)
    private CpuRestrictType cpuRestrictType = CpuRestrictType.ROUGH;
    /*precisely restrict the cpu speed,using the exact cpu clock speed and cpu core number*/
    @JSONField(serialize = false)
    private double cpuClockSpeed = 2.5;//GHz
    @JSONField(serialize = false)
    private int cpuCoreNumber = 1;//number of cores

    /*roughly restrict the cpu speed,using the percentage of the cpu usage rate*/
    private double cpuUsageRate = 8;//percentage of the cpu usage rate, 0.5 means 50%, it can be larger than 1(means using more than 1 core)

    /*restrict the memory size of the node*/
    private double memorySize = 8;//GB

    /*restrict the network bandwidth of the node*/
    private double networkOutBandwidth = 1024;//kbps
    private double networkOutLatency = 20;//ms

    private double networkInBandwidth = 1024;//kbps
    private double networkInLatency = 20;//ms

    private int fixedInLatency = 0;//ms, when this is not 0, the network in latency will be fixed to this value
    private int fixedOutLatency = 0;//ms, when this is not 0, the network out latency will be fixed to this value

    private double networkOutPacketLossRate = 0;//percentage of the packet loss rate

    @Override
    public String toString(){
        return "cpuRestrictType: " + getCpuRestrictType() + "\n" +
                "cpuClockSpeed: " + getCpuClockSpeed() + "\n" +
                "cpuCoreNumber: " + getCpuCoreNumber() + "\n" +
                "cpuUsageRate: " + getCpuUsageRate() + "\n" +
                "memorySize: " + getMemorySize() + "\n" +
                "networkOutBandwidth: " + getNetworkOutBandwidth() + "\n" +
                "networkOutLatency: " + getNetworkOutLatency() + "\n"+
                "networkInBandwidth: " + getNetworkInBandwidth() + "\n" +
                "networkInLatency: " + getNetworkInLatency() + "\n" +
                "fixedInLatency: " + getFixedInLatency() + "\n" +
                "fixedOutLatency: " + getFixedOutLatency() + "\n";
    }
}
