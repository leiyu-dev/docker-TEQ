package org.teq.presetlayers.taskInterface;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.presetlayers.PackageBean;

public interface EndDeviceTask{
    /**
     * you can do any computing task through this interface
     * @param packages
     * @return
     */

    DataStream<PackageBean> Computing(DataStream<PackageBean> packages);
    /**
     * you can use you way to show or store the query respond
     * @param respond
     */
    void Store(DataStream<PackageBean> respond);
}
