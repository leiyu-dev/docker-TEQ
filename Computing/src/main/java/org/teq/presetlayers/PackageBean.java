package org.teq.presetlayers;

import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.MetricsPackageBean;

import java.util.UUID;

/**
 * @warn must be serializable (all the elements should have full getters and setters)
 */
public class PackageBean extends MetricsPackageBean {
    public PackageBean(Object object) {
        super(object);
    }

    public PackageBean(String src, String target, InfoType type, Object object) {
        this(src,target,0,type,object);
    }
    public PackageBean(String src, String target, int targetPort, InfoType type, Object object) {
        super(src,target,targetPort,object, type);
    }
    public PackageBean(UUID id, String src, String target, int targetPort, InfoType type, Object object) {
        super(id,src,target,targetPort,object, type);
    }
}
