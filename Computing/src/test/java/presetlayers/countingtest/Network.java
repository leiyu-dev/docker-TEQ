package presetlayers.countingtest;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.TargetedDataSender;
import org.teq.utils.dataSet.dataSetPlayer.CommonDataSource;
import org.teq.utils.dataSet.dataSetPlayer.DataSetCommonPlayer;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CSVReader;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

import java.util.Arrays;
import java.util.Random;

import static org.teq.configurator.ExecutorParameters.coordinatorLayerName;

public class Network extends AbstractNetworkHostNode {
    private static final String filePath = "dataItem1M+ForLocal.csv";
    @Override
    public void dataProcess() {
        Random random = new Random();
        CommonReader<String[]> csvReader = new CSVReader( filePath, 30);
        CommonDataSource<String[]> dataSource = new DataSetCommonPlayer<String[]>().genPlayer( 10, csvReader);
        StreamExecutionEnvironment env = getEnv();
        env.addSource(dataSource).
                returns(String[].class).
                map((MapFunction<String[], PackageBean>) s -> new PackageBean(getNodeName(),
                        DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).get(
                                random.nextInt(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).size())),
                        ExecutorParameters.fromNetworkToEndPort, s[12].equals("0") ? InfoType.Data : InfoType.Query, s)).
                addSink(new TargetedDataSender<>(ExecutorParameters.maxNumRetries, ExecutorParameters.retryInterval)).setParallelism(1);
    }
}
