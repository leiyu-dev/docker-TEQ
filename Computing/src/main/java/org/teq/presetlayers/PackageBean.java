package org.teq.presetlayers;

import org.teq.configurator.unserializable.InfoType;
import org.teq.layer.mearsurer.MetricsPackageBean;

public class PackageBean extends MetricsPackageBean {
    public PackageBean(Object object) {
        super(object);
    }
    private String src;
    private String target;
    private InfoType type;
    public PackageBean(String src, String target, InfoType type, Object object) {
        super(object);
        this.src = src;
        this.target = target;
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public String getTarget() {
        return target;
    }

    public InfoType getType() {
        return type;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(InfoType object) {
        this.type = object;
    }
    @Override
    public String toString() {
        return "PackageBean{" +
                "id='" + getId() + '\'' +
                ", src='" + src + '\'' +
                ", target='" + target + '\'' +
                ", type=" + type +
                ", object=" + getObject() +
                '}';
    }

}
