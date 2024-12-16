package org.teq.mearsurer;

import org.teq.configurator.unserializable.InfoType;

import java.io.Serializable;
import java.util.UUID;

public class BuiltInMetrics implements Serializable {
    private UUID id;
    private int fromNodeId;
    private int toNodeId;
    private double packageLength;//unit: B
    private long timestampIn;
    private long timestampOut;
    private InfoType infoType;
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }
    public void setPackageLength(double packageLength) {
        this.packageLength = packageLength;
    }
    public double getPackageLength() {
        return packageLength;
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

    public int getFromNodeId() {
        return fromNodeId;
    }
    public void setFromNodeId(int fromNodeId) {
        this.fromNodeId = fromNodeId;
    }
    public int getToNodeId() {
        return toNodeId;
    }
    public void setToNodeId(int toNodeId) {
        this.toNodeId = toNodeId;
    }
    public InfoType getInfoType() {
        return infoType;
    }
    public void setInfoType(InfoType infoType) {
        this.infoType = infoType;
    }
    @Override
    public String toString() {
        return "BuiltInMetrics{" +
                "id='" + id + '\'' +
                ", packageLength=" + packageLength +
                ", timestampIn=" + timestampIn +
                ", timestampOut=" + timestampOut +
                ", fromNodeId=" + fromNodeId +
                ", toNodeId=" + toNodeId +
                ", infoType=" + infoType +
                '}';
    }


}
