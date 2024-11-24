package org.teq.layer.mearsurer;

import org.bouncycastle.asn1.eac.PackedDate;

import java.io.Serializable;

public class PackageBean implements Serializable {
    static private long instanceCount = 0;
    private long id;
    private Object object;

    public PackageBean(long id, Object object) {
        this.id = id;
        this.object = object;
        instanceCount++;
    }

    public PackageBean(Object object){
        this(instanceCount, object);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
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
