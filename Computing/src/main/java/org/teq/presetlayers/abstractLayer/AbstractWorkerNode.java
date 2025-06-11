package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.*;
import org.teq.configurator.ExecutorConfig;
import org.teq.mearsurer.MeasuredFlinkNode;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.WorkerTask;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.MultiThreadDataReceiver;
import org.teq.utils.connector.flink.javasocket.TargetedDataSender;

public abstract class AbstractWorkerNode extends MeasuredFlinkNode implements WorkerTask  {
    protected String logConfigFilePath; // 每个实例的日志配置文件路径都不一样
    public void setLogFilePath(String path) {
        logConfigFilePath = path;
    }
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceNode.class);

    private DataStream<PackageBean> FromCenter;
    private DataStream<PackageBean> FromCod;
    public DataStream<PackageBean> getFromCenterStream() {
        return FromCenter;
    }
    public DataStream<PackageBean> getFromCodStream() {
        return FromCod;
    }

    @Override
    public void dataProcess() throws Exception {
        int maxNumRetries = ExecutorConfig.maxNumRetries;
        int retryInterval = ExecutorConfig.retryInterval;
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromCenter = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorConfig.fromCenterToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));

        DataStream<PackageBean> FromCod =  env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorConfig.fromCodToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));

        DataStream<PackageBean> info = FromCenter.union(FromCod);
        DataStream<PackageBean> transformedWorkers = measureWorkerLayerRecord(info);

        DataStreamSink sink = transformedWorkers.addSink(new TargetedDataSender<>(maxNumRetries,retryInterval));
    }
    public DataStream<PackageBean> measureWorkerLayerRecord(DataStream<PackageBean> stream) {
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), packageBean.getTimestampOut(),packageBean.getSrc(),packageBean);
                logger.trace("Worker Layer received data: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        // 处理逻辑
        DataStream<PackageBean> transformedWorkers = transform(inputMap);
        return transformedWorkers.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                if(DockerRuntimeData.getLayerNameByNodeName(packageBean.getTarget()).equals(ExecutorConfig.dataCenterLayerName))
                    packageBean.setTargetPort(ExecutorConfig.fromWorkerToCenterPort);
                else
                    packageBean.setTargetPort(ExecutorConfig.fromWorkerToCodPort);
                packageBean.setSrc(getNodeName());
                logger.trace("Worker Layer send data: {}", packageBean);

                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()),
                        JSON.toJSONString(packageBean).length() * 2, packageBean.getType(),packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}

