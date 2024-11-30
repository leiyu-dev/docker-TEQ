package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractWorkerLayer;
import org.teq.utils.DockerRuntimeData;

public class WorkerLayer extends AbstractWorkerLayer {
    @Override
    public DataStream<PackageBean> transform(DataStream<PackageBean>info) {
        return info.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean value) throws Exception {
                if(DockerRuntimeData.getLayerNameByNodeName(value.getSrc()).equals(ExecutorParameters.dataCenterLayerName)){
                    value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.coordinatorLayerName).get(0));
                } else{ //coordinator
                    Thread.sleep(getNodeID()* 10L);
                    value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.dataCenterLayerName).get(0));
                }
                return value;
            }
        });
    }
}
