package org.teq.layer.mearsurer;

import java.io.Serializable;
import java.util.UUID;

public class MetricsPackageBean implements Serializable {
    static private long instanceCount = 0;
    private UUID id;
    private Object object;

    /* src, targetPort and target are used to send messages between different nodes*/
    private String src;
    private String target;
    private int targetPort;

    public MetricsPackageBean(Object object) {
        this.id = UUID.randomUUID();
        this.object = object;
        instanceCount++;
    }

    public MetricsPackageBean(String src, String target, int targetPort,Object object) {
        this.id = UUID.randomUUID();
        this.object = object;
        this.src = src;
        this.target = target;
        this.targetPort = targetPort;
        instanceCount++;
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

    public String toString(){
        return "PackageBean{" +
                "id='" + id + '\'' +
                ", object=" + object +
                ", src='" + src + '\'' +
                ", target='" + target + '\'' +
                ", targetPort=" + targetPort +
                '}';
    }
}
