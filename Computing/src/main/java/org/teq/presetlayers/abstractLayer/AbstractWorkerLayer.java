package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.logging.log4j.*;
import org.teq.configurator.ExecutorParameters;
import org.teq.layer.mearsurer.MeasuredFlinkNode;
import org.teq.configurator.unserializable.DevicePrefixName;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.WorkerTask;
import org.teq.presetlayers.utils.PhysicalPartition;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.*;

public abstract class AbstractWorkerLayer extends MeasuredFlinkNode implements WorkerTask  {
    protected String logConfigFilePath; // 每个实例的日志配置文件路径都不一样
    public void setLogFilePath(String path) {
        logConfigFilePath = path;
    }
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);


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
                beginProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2);
                logger.debug("Worker Layer received data: {}", packageBean);
                return packageBean;
            }
        });
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

                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                return packageBean;
            }
        });
    }
}

