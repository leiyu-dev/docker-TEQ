package org.teq.presetlayers.taskInterface;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.teq.presetlayers.PackageBean;

public interface WorkerTask{
    public DataStream<PackageBean> transform(KeyedStream<PackageBean, Integer> info);
}
