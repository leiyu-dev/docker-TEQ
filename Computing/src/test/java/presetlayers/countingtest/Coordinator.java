package presetlayers.countingtest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractCoordinatorNode;
import org.teq.utils.DockerRuntimeData;

public class Coordinator extends AbstractCoordinatorNode {
    private static final Logger logger = LogManager.getLogger(Coordinator.class);

    /**
     * if there are too many sensors, we can use this method to load the graph
     */
   /* private static void loadGraph(int sensorNum, int workNum) {
        int avgSensor = sensorNum / workNum;
        int offset = 0;
        // 前面的workNum - 1分配给 avgSensor
        for (int i = 0; i < workNum - 1; i++) {
            Integer worker = i;
            List<String> ss = new ArrayList<>();
            for (int j = 0; j < avgSensor; j++) {
                ss.add(SPrefix + (j + offset));
                routerTable.put((SPrefix + (j + offset)), WPrefix + worker);
            }
            graph.put(worker, ss);
            offset += avgSensor;
        }
        // 剩余的
        Integer worker = workNum - 1;
        List<String> ss = new ArrayList<>();
        for (int j = 0; j < avgSensor + sensorNum % workNum; j++){
            ss.add(SPrefix + (j + offset));
            routerTable.put((SPrefix + (j + offset)), WPrefix + worker);
        }
        graph.put(worker, ss);
    }*/
    @Override
    public DataStream<PackageBean> Routing(DataStream<PackageBean> info) {
        int workerNum = DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).size();
        return info.map((MapFunction<PackageBean, PackageBean>) packageBean -> {
            int sensorId = DockerRuntimeData.getNodeRankInLayer(packageBean.getSrc(),ExecutorParameters.endDeviceLayerName); // value: 0 - 15
            packageBean.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.workerLayerName).get(sensorId % workerNum)); // worker0,1,2,3
            return packageBean;
        });
    }

    @Override
    public DataStream<PackageBean> SendBack(DataStream<PackageBean> backInfo) {
        return backInfo.map((MapFunction<PackageBean, PackageBean>) value -> {
            value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).get(0));
            return value;
        });
    }
}
