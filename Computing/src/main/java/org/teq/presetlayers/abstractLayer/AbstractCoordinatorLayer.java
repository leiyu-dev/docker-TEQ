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
import org.teq.presetlayers.taskInterface.CoordinatorTask;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;
//import org.apache.log4j.PropertyConfigurator;


public abstract class AbstractCoordinatorLayer extends MeasuredFlinkNode implements CoordinatorTask {
    protected String logConfigFilePath; // 每个实例的日志配置文件路径都不一样
    public void setLogFilePath(String path) {
        logConfigFilePath = path;
    }
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);

    public AbstractCoordinatorLayer(){}

    public void execute() throws Exception {
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        // PropertyConfigurator.configure(logConfigFilePath);
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromEnd = env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromEndToCodPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> FromWorker = env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromWorkerToCodPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> routedInfo = measureCoordinatorRecord(FromEnd);
        DataStreamSink<PackageBean> ToEnd = FromWorker.addSink(new CommonDataSender("localhost", ExecutorParameters.fromCodToEndPort, maxNumRetries,retryInterval)).setParallelism(1);
        logger.info("CoordinatorLayer: ToEnd port is {}", ExecutorParameters.fromCodToEndPort);
        DataStreamSink<PackageBean> ToWorker = routedInfo.addSink(new CommonDataSender("localhost",ExecutorParameters.fromCodToWorkerPort, maxNumRetries,retryInterval)).setParallelism(1);
        logger.info("CoordinatorLayer: ToWorker port is {}", ExecutorParameters.fromCodToWorkerPort);
        env.executeAsync();

    }
    public DataStream<PackageBean> measureCoordinatorRecord(DataStream<PackageBean> stream){
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2);
                logger.debug("Coordinator Layer received data from End Device: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> routedMap = Routing(inputMap);
        return routedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                logger.debug("Coordinator Layer sent data to Worker: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
