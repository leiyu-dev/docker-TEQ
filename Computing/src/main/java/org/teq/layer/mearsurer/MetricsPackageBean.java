package org.teq.layer.mearsurer;

import java.io.Serializable;
import java.util.UUID;

public class MetricsPackageBean implements Serializable {
    static private long instanceCount = 0;
    private final UUID id;
    private Object object;

    public MetricsPackageBean(Object object) {
        this.id = UUID.randomUUID();
        this.object = object;
        instanceCount++;
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

    public String toString(){
        return "PackageBean{" +
                "id='" + id + '\'' +
                ", object=" + object +
                '}';
    }
}
