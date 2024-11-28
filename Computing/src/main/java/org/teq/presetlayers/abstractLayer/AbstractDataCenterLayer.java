package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.layer.mearsurer.MeasuredFlinkNode;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.DataCenterTask;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;

public abstract class AbstractDataCenterLayer extends MeasuredFlinkNode implements DataCenterTask {
    protected String logConfigFilePath; // 每个实例的日志配置文件路径都不一样
    public void setLogFilePath(String path) {
        logConfigFilePath = path;
    }
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);

    public AbstractDataCenterLayer(){}

    @Override
    public void dataProcess() throws Exception {
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromWorker = env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromWorkerToCenterPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> modifiedInfo = measurerDataCenterRecord(FromWorker);
        DataStreamSink ToWorker = modifiedInfo.addSink(new CommonDataSender("localhost", ExecutorParameters.fromCenterToWorkerPort, maxNumRetries,retryInterval)).setParallelism(1);
        env.executeAsync();
    }
    public DataStream<PackageBean> measurerDataCenterRecord(DataStream<PackageBean> stream){
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2);
                logger.debug("DataCenterLayer: Received data from Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> modifiedMap = transform(inputMap);
        return modifiedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                logger.debug("DataCenterLayer: Sent data to Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
