package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.*;
import org.teq.configurator.ExecutorParameters;
import org.teq.mearsurer.MeasuredFlinkNode;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.WorkerTask;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.*;

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
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromCenter = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromCenterToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));

        DataStream<PackageBean> FromCod =  env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromCodToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));

        DataStream<PackageBean> info = FromCenter.union(FromCod);
        DataStream<PackageBean> transformedWorkers = measureWorkerLayerRecord(info);

        DataStreamSink sink = transformedWorkers.addSink(new TargetedDataSender<>(maxNumRetries,retryInterval));
    }
    public DataStream<PackageBean> measureWorkerLayerRecord(DataStream<PackageBean> stream) {
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId());
                logger.debug("Worker Layer received data: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        // 处理逻辑
        DataStream<PackageBean> transformedWorkers = transform(inputMap);
        return transformedWorkers.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                if(DockerRuntimeData.getLayerNameByNodeName(packageBean.getTarget()).equals(ExecutorParameters.dataCenterLayerName))
                    packageBean.setTargetPort(ExecutorParameters.fromWorkerToCenterPort);
                else
                    packageBean.setTargetPort(ExecutorParameters.fromWorkerToCodPort);
                packageBean.setSrc(getNodeName());
                logger.debug("Worker Layer send data: {}", packageBean);

                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()),
                        JSON.toJSONString(packageBean).length() * 2, packageBean.getType());
                return packageBean;
            }
        }).setParallelism(1);
    }
}

