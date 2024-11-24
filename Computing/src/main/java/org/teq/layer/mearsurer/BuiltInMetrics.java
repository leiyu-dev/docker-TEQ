package org.teq.layer.mearsurer;

import java.io.Serializable;

public class BuiltInMetrics implements Serializable {
    private long id;
    private String fromNode;
    private double packageLength;//unit: kb
    private double memoryUsage;//unit Mb
    private double cpuUsage;//unit: %
    private long timestampIn;
    private long timestampOut;
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
    public void setPackageLength(double packageLength) {
        this.packageLength = packageLength;
    }
    public double getPackageLength() {
        return packageLength;
    }
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    public double getMemoryUsage() {
        return memoryUsage;
    }
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    public double getCpuUsage() {
        return cpuUsage;
    }
    public void setTimestampIn(long timestampIn) {
        this.timestampIn = timestampIn;
    }
    public long getTimestampIn() {
        return timestampIn;
    }
    public void setTimestampOut(long timestampOut) {
        this.timestampOut = timestampOut;
    }
    public long getTimestampOut() {
        return timestampOut;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getFromNode() {
        return fromNode;
    }

    @Override
    public String toString() {
        return "BuiltInMetrics{" +
                "id='" + id + '\'' +
                ", packageLength=" + packageLength +
                ", memoryUsage=" + memoryUsage +
                ", cpuUsage=" + cpuUsage +
                ", timestampIn=" + timestampIn +
                ", timestampOut=" + timestampOut +
                ", fromNode='" + fromNode + '\'' +
                '}';
    }
}
