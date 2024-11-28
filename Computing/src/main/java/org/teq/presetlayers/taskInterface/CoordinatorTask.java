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
}
