package example;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.ExecutorConfig;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractEndDeviceNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.netty.HighPerformanceDataReceiver;

import java.util.Arrays;
import java.util.List;

import static org.teq.configurator.ExecutorConfig.coordinatorLayerName;

public class EndDevice extends AbstractEndDeviceNode {
    private static final Logger logger = LogManager.getLogger(EndDevice.class);

    private static final String filePath = "dataItem1M+ForLocal.csv";
    @Override
    protected DataStream<PackageBean> getSource() {
        StreamExecutionEnvironment env = getEnv();
        return env.addSource(new HighPerformanceDataReceiver<PackageBean>(ExecutorConfig.fromNetworkToEndPort, PackageBean.class)).
                returns(PackageBean.class).
                map(new MapFunction<PackageBean, PackageBean>() {
                    @Override
                    public PackageBean map(PackageBean packageBean) throws Exception {
                        String[] s = ((List<String>)packageBean.getObject()).toArray(new String[0]);
                    return new PackageBean(getNodeName(),
                            DockerRuntimeData.getNodeNameListByLayerName(coordinatorLayerName).get(0),
                            ExecutorConfig.fromEndToCodPort, s[12].equals("0") ? InfoType.Data : InfoType.Query, Arrays.asList(s),
                            packageBean.getTimestampOut());
                    }
                });
//                map((MapFunction<PackageBean, String[]>) s -> ((List<String>)s.getObject()).toArray(new String[0])).
//                map((MapFunction<String[], PackageBean>) s -> {
////                    logger.trace("EndDevice: " + Arrays.asList(s));
//                    return new PackageBean(getNodeName(),
//                            DockerRuntimeData.getNodeNameListByLayerName(coordinatorLayerName).get(0),
//                            ExecutorParameters.fromEndToCodPort, s[12].equals("0") ? InfoType.Data : InfoType.Query, Arrays.asList(s));
//                });
    }

    @Override
    public DataStream<PackageBean> Computing(DataStream<PackageBean> packages) {
        return packages;
    }

    @Override
    public void Store(DataStream<PackageBean> respond) {
        DataStream<String> output = respond.map(new MapFunction<PackageBean, String>() {
            @Override
            public String map(PackageBean brokerBean) throws Exception {
                List<String> object = JSONObject.parseArray(brokerBean.getObject().toString(), String.class);
                StringBuilder stringBuilder = new StringBuilder("GET RESPONSE: ");
                stringBuilder.append(brokerBean.getSrc()).append(",");
                for (String temp : object) {
                    stringBuilder.append(temp).append(",");
                }
                // stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                return stringBuilder.toString();
            }
        });
        output.print().setParallelism(1);
    }
}
