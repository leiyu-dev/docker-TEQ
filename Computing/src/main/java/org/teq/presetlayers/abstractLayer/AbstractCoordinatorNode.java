package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.mearsurer.MeasuredFlinkNode;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.CoordinatorTask;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.MultiThreadDataReceiver;
import org.teq.utils.connector.flink.javasocket.TargetedDataSender;
//import org.apache.log4j.PropertyConfigurator;


public abstract class AbstractCoordinatorNode extends MeasuredFlinkNode implements CoordinatorTask {
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceNode.class);

    public AbstractCoordinatorNode(){}

    @Override
    public void dataProcess() throws Exception {
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        StreamExecutionEnvironment env = getEnv();

        DataStream<PackageBean> FromEnd = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromEndToCodPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> routedToWorker = measureToWorkerRecord(FromEnd);

        DataStream<PackageBean> FromWorker = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromWorkerToCodPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> routedToEnd = measureToEndRecord(FromWorker);


        DataStreamSink<PackageBean> ToEnd = routedToEnd.addSink(new TargetedDataSender<>(maxNumRetries,retryInterval)).setParallelism(1);
        DataStreamSink<PackageBean> ToWorker = routedToWorker.addSink(new TargetedDataSender<>(maxNumRetries,retryInterval)).setParallelism(1);
    }
    public DataStream<PackageBean> measureToWorkerRecord(DataStream<PackageBean> stream){
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), packageBean.getTimestampOut(),packageBean.getSrc(),packageBean);
                logger.debug("Coordinator Layer received data from End Device: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> routedMap = Routing(inputMap);
        return routedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                packageBean.setSrc(getNodeName());
                packageBean.setTargetPort(ExecutorParameters.fromCodToWorkerPort);
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()),
                        JSON.toJSONString(packageBean).length() * 2, packageBean.getType(),packageBean);
                logger.debug("Coordinator Layer sent data to Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }

    public DataStream<PackageBean> measureToEndRecord(DataStream<PackageBean> stream){
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), packageBean.getTimestampOut(),packageBean.getSrc(),packageBean);
                logger.debug("Coordinator Layer received data from End Device: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> routedMap = SendBack(inputMap);
        return routedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                packageBean.setSrc(getNodeName());
                packageBean.setTargetPort(ExecutorParameters.fromCodToEndPort);
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()),
                        JSON.toJSONString(packageBean).length() * 2, packageBean.getType(),packageBean);
                logger.debug("Coordinator Layer sent data to Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
