package org.teq.mearsurer;

import org.teq.configurator.unserializable.InfoType;

import java.io.Serializable;
import java.util.UUID;

public class MetricsPackageBean implements Serializable {
    protected InfoType type;
    private UUID id;
    private Object object;

    /* src, targetPort and target are used to send messages between different nodes*/
    private String src;
    private String target;
    private int targetPort;

    /**
     * timestampOut is used to store the time when the message was sent out, used to fix the transfer latency
     */
    private long timestampOut;

    public MetricsPackageBean(Object object) {
        this.id = UUID.randomUUID();
        this.object = object;
    }

    public MetricsPackageBean(String src, String target, int targetPort, Object object, InfoType type) {
        this.id = UUID.randomUUID();
        this.object = object;
        this.src = src;
        this.target = target;
        this.targetPort = targetPort;
        this.type = type;
    }

    public MetricsPackageBean(UUID id, String src, String target, int targetPort, Object object, InfoType type) {
        this.id = id;
        this.object = object;
        this.src = src;
        this.target = target;
        this.targetPort = targetPort;
        this.type = type;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getId() {
        return id;
    }
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    public void setSrc(String src) {
        this.src = src;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getSrc() {
        return src;
    }

    public String getTarget() {
        return target;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }
    public InfoType getType() {
        return type;
    }
    public void setType(InfoType object) {
        this.type = object;
    }
    public String toString(){
        return "PackageBean{" +
                "id='" + id + '\'' +
                ", object=" + object +
                ", src='" + src + '\'' +
                ", target='" + target + '\'' +
                ", targetPort=" + targetPort +
                ", type=" + type +
                ", timestampOut=" + timestampOut +
                '}';
    }

    public long getTimestampOut() {
        return timestampOut;
    }

    public void setTimestampOut(long timestampOut) {
        this.timestampOut = timestampOut;
    }
}
