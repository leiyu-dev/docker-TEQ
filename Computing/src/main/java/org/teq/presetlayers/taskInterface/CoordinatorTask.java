package org.teq.presetlayers.taskInterface;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.presetlayers.PackageBean;

public interface CoordinatorTask {
    /**
     * 这个接口用来定义你的路由规则
     * @param info
     * @return
     */
    DataStream<PackageBean> Routing(DataStream<PackageBean> info);

    /**
     * this interface is used to define your send back rule (to the EndDeviceLayer)
     * @param backInfo
     * @return
     */
    DataStream<PackageBean> SendBack(DataStream<PackageBean> backInfo);
}
