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
import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.MeasuredFlinkNode;
import org.teq.configurator.ExecutorParameters;
import org.teq.presetlayers.PackageBean;
import org.teq.presetlayers.taskInterface.EndDeviceTask;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.MultiThreadDataReceiver;
import org.teq.utils.connector.TargetedDataSender;


public abstract class AbstractEndDeviceNode extends MeasuredFlinkNode implements EndDeviceTask {
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceNode.class);

    protected abstract DataStream<PackageBean> getSource();

    @Override
    public void dataProcess() throws Exception {
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> fromSensor = getSource();
        DataStream<PackageBean> response = env.addSource(new MultiThreadDataReceiver<PackageBean>(ExecutorParameters.fromCodToEndPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));


        DataStream<PackageBean> computedStream = measureDataStream(fromSensor);
        measureResponseDataStream(response);

        DataStreamSink<PackageBean> ToCod = computedStream.addSink(new TargetedDataSender<>(ExecutorParameters.maxNumRetries, ExecutorParameters.retryInterval)).setParallelism(1);
        logger.info("EndDeviceLayer: ToCod port is {}", ExecutorParameters.fromEndToCodPort);
    }
    public DataStream<PackageBean> measureDataStream(DataStream<PackageBean> infoSteam) {
        DataStream<PackageBean> inputMap = infoSteam.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId());
                logger.debug("End Device Layer received data from Sensor: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        DataStream<PackageBean> computedMap = Computing(inputMap);
        return computedMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                packageBean.setSrc(getNodeName());
                packageBean.setTargetPort(ExecutorParameters.fromEndToCodPort);
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()),JSON.toJSONString(packageBean).length() * 2, packageBean.getType());
                logger.debug("End Device Layer send data to Coordinator: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
    public void measureResponseDataStream(DataStream<PackageBean> infoSteam) {
        DataStream<PackageBean> inputMap = infoSteam.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId());
                logger.debug("End Device Layer received data from Sensor: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
        Store(inputMap);
        inputMap.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                packageBean.setSrc(getNodeName());
                endProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2, packageBean.getType());
                logger.debug("End Device Layer send data to Coordinator: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}
