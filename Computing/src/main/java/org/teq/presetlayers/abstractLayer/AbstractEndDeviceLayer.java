package org.teq.presetlayers.abstractLayer;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.layer.mearsurer.MeasuredFlinkNode;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.EndDeviceTask;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;


public abstract class AbstractEndDeviceLayer extends MeasuredFlinkNode implements EndDeviceTask {
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);

    protected abstract DataStream<PackageBean> getSource();

    @Override
    public void dataProcess() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> infoStream = getSource();
        DataStream<PackageBean> computedStream = measureDataStream(infoStream);
        DataStream<PackageBean> response = env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromCodToEndPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        measureDataStream(response);
        Store(response);
        DataStreamSink<PackageBean> ToCod = computedStream.addSink(new CommonDataSender("localhost", ExecutorParameters.fromEndToCodPort, ExecutorParameters.maxNumRetries, ExecutorParameters.retryInterval)).setParallelism(1);
        logger.info("EndDeviceLayer: ToCod port is {}", ExecutorParameters.fromEndToCodPort);
    }
    public DataStream<PackageBean> measureDataStream(DataStream<PackageBean> infoSteam) {
        DataStream<PackageBean> inputMap = infoSteam.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2);
                logger.debug("End Device Layer received data from Sensor: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> computedMap = Computing(inputMap);
        return computedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                logger.debug("End Device Layer send data to Coordinator: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
