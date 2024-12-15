package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractCoordinatorNode;
import org.teq.utils.DockerRuntimeData;

public class CoordinatorNode extends AbstractCoordinatorNode {


    @Override
    public DataStream<PackageBean> Routing(DataStream<PackageBean> info) {
        int workerNum = DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).size();
        return info.map((MapFunction<PackageBean, PackageBean>) packageBean -> {
            int sensorId = DockerRuntimeData.getNodeRankInLayer(packageBean.getSrc(),ExecutorParameters.endDeviceLayerName); // value: 0 - 15
            packageBean.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).get(sensorId / workerNum)); // worker0,1,2,3
            Thread.sleep(1000);
            return packageBean;
        });
    }

    @Override
    public DataStream<PackageBean> SendBack(DataStream<PackageBean> backInfo) {
        return backInfo.map((MapFunction<PackageBean, PackageBean>) value -> {
            value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).get(0));
            Thread.sleep(1000);
            return value;
        });
    }

}
