package org.teq.presetlayers.taskInterface;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.presetlayers.PackageBean;

public interface DataCenterTask {
    public DataStream<PackageBean> transform(DataStream<PackageBean> info);
}

