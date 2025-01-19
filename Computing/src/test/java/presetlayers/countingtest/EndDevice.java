package presetlayers.countingtest;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.abstractLayer.AbstractEndDeviceNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.MultiThreadDataReceiver;
import org.teq.utils.dataSet.dataSetPlayer.CommonDataSource;
import org.teq.utils.dataSet.dataSetPlayer.DataSetCommonPlayer;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CSVReader;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

import java.util.Arrays;
import java.util.List;

import static org.teq.configurator.ExecutorParameters.coordinatorLayerName;

public class EndDevice extends AbstractEndDeviceNode {

    private static final String filePath = "dataItem1M+ForLocal.csv";
    @Override
    protected DataStream<PackageBean> getSource() {
        StreamExecutionEnvironment env = getEnv();
        return env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromNetworkToEndPort, PackageBean.class)).
                returns(PackageBean.class).
                map((MapFunction<PackageBean, String[]>) s -> (String[])s.getObject()).
                map((MapFunction<String[], PackageBean>) s -> new PackageBean(s[14],
                        DockerRuntimeData.getNodeNameListByLayerName(coordinatorLayerName).get(0),
                        ExecutorParameters.fromEndToCodPort, s[12].equals("0") ? InfoType.Data : InfoType.Query, Arrays.asList(s)));
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
