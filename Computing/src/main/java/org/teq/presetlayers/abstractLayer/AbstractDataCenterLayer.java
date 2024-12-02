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
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;
import org.teq.utils.connector.MultiThreadDataReceiver;
import org.teq.utils.connector.TargetedDataSender;

public abstract class AbstractDataCenterLayer extends MeasuredFlinkNode implements DataCenterTask {
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);
    @Override
    public void dataProcess() throws Exception {
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromWorker = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromWorkerToCenterPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> modifiedInfo = measurerDataCenterRecord(FromWorker);
        DataStreamSink ToWorker = modifiedInfo.addSink(new TargetedDataSender<>(maxNumRetries,retryInterval));
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
                packageBean.setTargetPort(ExecutorParameters.fromCenterToWorkerPort);
                packageBean.setSrc(getNodeName());
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                logger.debug("DataCenterLayer: Sent data to Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
