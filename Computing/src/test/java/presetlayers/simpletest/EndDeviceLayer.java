package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.presetlayers.abstractLayer.AbstractEndDeviceLayer;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;

public class EndDeviceLayer extends AbstractEndDeviceLayer {
    @Override
    protected DataStream<PackageBean> getSource() {
        String filePath = "./file.txt";
        StreamExecutionEnvironment env = getEnv();
        DataStream<String> input = env.readTextFile(filePath);
        return input.map(new MapFunction<String, PackageBean>() {
            @Override
            public PackageBean map(String value) throws Exception {
                return new PackageBean(value);
            }
        });
    }
    @Override
    public DataStream<PackageBean> Computing(DataStream<PackageBean> packages) {
        return null;
    }

    @Override
    public void Store(DataStream<PackageBean> respond) {

    }
}
