package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.abstractLayer.AbstractEndDeviceNode;
import org.teq.configurator.ExecutorConfig;
import org.teq.presetlayers.PackageBean;
import org.teq.utils.DockerRuntimeData;

public class EndDeviceNode extends AbstractEndDeviceNode {
    @Override
    protected DataStream<PackageBean> getSource() {
        String filePath = "./file.txt";
        StreamExecutionEnvironment env = getEnv();
        if(DockerRuntimeData.getNodeNameListByLayerName(ExecutorConfig.endDeviceLayerName).get(0).equals(getNodeName())) {//only the first end device node reads the file
            DataStream<String> input = env.readTextFile(filePath);
            return input.map((MapFunction<String, PackageBean>) value -> new PackageBean(getNodeName(),
                    DockerRuntimeData.getNodeNameListByLayerName(ExecutorConfig.coordinatorLayerName).get(0),
                    InfoType.Data, value)).name("Data Source");
        }
        else { // return an empty data stream
            return env.fromElements();
        }
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
