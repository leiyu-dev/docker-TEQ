package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.abstractLayer.AbstractEndDeviceLayer;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.utils.DockerRuntimeData;

public class EndDeviceLayer extends AbstractEndDeviceLayer {
    @Override
    protected DataStream<PackageBean> getSource() {
        String filePath = "./file.txt";
        StreamExecutionEnvironment env = getEnv();
        DataStream<String> input = env.readTextFile(filePath);
        return input.map((MapFunction<String, PackageBean>) value -> new PackageBean(getNodeName(),
                DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.coordinatorLayerName).get(0),
                InfoType.Data,value)).name("Data Source");
    }
    @Override
    public DataStream<PackageBean> Computing(DataStream<PackageBean> packages) {
        return packages.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean value) throws Exception {
                Thread.sleep(1000);
                return value;
            }
        });
    }

    @Override
    public void Store(DataStream<PackageBean> respond) {
        respond.map(new MapFunction<PackageBean, PackageBean>() {

            @Override
            public PackageBean map(PackageBean value) throws Exception {
                System.out.println(value.getObject());
                return value;
            }
        }).name("Store");
    }
}
