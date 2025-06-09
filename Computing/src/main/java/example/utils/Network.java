package example.utils;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.unserializable.InfoType;
import org.teq.presetlayers.PackageBean;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.TargetedDataSender;
import org.teq.utils.dataSet.dataSetPlayer.CommonDataSource;
import org.teq.utils.dataSet.dataSetPlayer.DataSetCommonPlayer;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CSVReader;
import org.teq.utils.dataSet.dataSetPlayer.Reader.CommonReader;

import java.util.Arrays;
import java.util.Random;

public class Network extends AbstractNetworkHostNode {
    private static final String filePath = "dataItem1M+ForLocal.csv";

    /**
     * this method is used to generate data from the csv file, and it will send them separately to the end devices
     */
    @Override
    public void dataProcess() {
        //wait for 10 seconds to make sure the end devices are ready
        try {
            Thread.sleep(ExecutorParameters.waitBeforeStart);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        CommonReader<String[]> csvReader = new CSVReader( filePath, 30);
        CommonDataSource<String[]> dataSource = new DataSetCommonPlayer<String[]>().genPlayer( csvReader);
        StreamExecutionEnvironment env = getEnv();
        env.addSource(dataSource).
                returns(String[].class).
                map((MapFunction<String[], PackageBean>) s -> new PackageBean(getNodeName(),
                        DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).get(
                                random.nextInt(DockerRuntimeData.getNodeNameListByLayerName(ExecutorParameters.endDeviceLayerName).size())),
                        ExecutorParameters.fromNetworkToEndPort, s[12].equals("0") ? InfoType.Data : InfoType.Query, Arrays.asList(s), System.nanoTime())).
                addSink(new TargetedDataSender<>(ExecutorParameters.maxNumRetries, ExecutorParameters.retryInterval)).setParallelism(1);
    }
}
