package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractDataCenterNode;

public class DataCenterNode extends AbstractDataCenterNode {

    @Override
    public DataStream<PackageBean> transform(DataStream<PackageBean> info) {
        return info.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean value) throws Exception {
                value.setTarget(value.getSrc());
                Thread.sleep(1000);
                return value;
            }
        });
    }
}
