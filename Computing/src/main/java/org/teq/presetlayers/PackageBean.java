package org.teq.presetlayers;

import org.bouncycastle.util.Pack;
import org.teq.configurator.unserializable.InfoType;
import org.teq.layer.mearsurer.MetricsPackageBean;
import scala.concurrent.java8.FuturesConvertersImpl;

public class PackageBean extends MetricsPackageBean {
    public PackageBean(Object object) {
        super(object);
    }

    private InfoType type;
    public PackageBean(String src, String target, InfoType type, Object object) {
        this(src,target,0,type,object);
    }
    public PackageBean(String src, String target, int targetPort, InfoType type, Object object) {
        super(src,target,targetPort,object);
        this.type = type;
    }


    public InfoType getType() {
        return type;
    }
    public void setType(InfoType object) {
        this.type = object;
    }
    @Override
    public String toString() {
        return "PackageBean{" +
                "id='" + getId() + '\'' +
                ", src='" + getSrc() + '\'' +
                ", target='" + getTarget() + '\'' +
                ", targetPort=" + getTargetPort() +
                ", type=" + type +
                ", object=" + getObject() +
                '}';
    }
}
