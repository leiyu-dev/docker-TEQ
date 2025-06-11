package presetlayers.simpletest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.configurator.ExecutorConfig;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractWorkerNode;
import org.teq.utils.DockerRuntimeData;

public class WorkerNode extends AbstractWorkerNode {
    @Override
    public DataStream<PackageBean> transform(DataStream<PackageBean>info) {
        return info.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean value) throws Exception {
                if(DockerRuntimeData.getLayerNameByNodeName(value.getSrc()).equals(ExecutorConfig.dataCenterLayerName)){
                    value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorConfig.coordinatorLayerName).get(0));
                } else{ //coordinator
                    Thread.sleep(1000);
                    value.setTarget(DockerRuntimeData.getNodeNameListByLayerName(ExecutorConfig.dataCenterLayerName).get(0));
                }
                return value;
            }
        });
    }
}
//tc qdisc add dev eth0 root netem delay 1000ms
//teq_node_EndDeviceLayer-0
//docker run --privileged --cap-add=NET_ADMIN --sysctl net.ipv6.conf.all.disable_ipv6=0 --network teq-network -it teq:1.1 test /bin/bash