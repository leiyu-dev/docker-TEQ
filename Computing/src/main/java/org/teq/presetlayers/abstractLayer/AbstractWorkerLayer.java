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
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.*;

public abstract class AbstractWorkerLayer extends MeasuredFlinkNode implements WorkerTask  {
    protected String logConfigFilePath; // 每个实例的日志配置文件路径都不一样
    public void setLogFilePath(String path) {
        logConfigFilePath = path;
    }
    private static final Logger logger = LogManager.getLogger(AbstractEndDeviceLayer.class);


    @Override
    public void dataProcess() throws Exception {
        int workerNum = ExecutorParameters.workersNum;
        int maxNumRetries = ExecutorParameters.maxNumRetries;
        int retryInterval = ExecutorParameters.retryInterval;
        StreamExecutionEnvironment env = getEnv();
        DataStream<PackageBean> FromCenter = env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromCenterToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> FromCod =  env.addSource(new CommonDataReceiver<PackageBean>(ExecutorParameters.fromCodToWorkerPort, PackageBean.class))
                .returns(TypeInformation.of(PackageBean.class));
        DataStream<PackageBean> info = FromCenter.union(FromCod);
        DataStream<PackageBean> transformedWorkers = measureWorkerLayerRecord(info, workerNum);
        // 按照现在workers的target 来切分为两个流发给Cod 和 Center
        OutputTag<PackageBean> outputTag = new OutputTag<>(DevicePrefixName.Center.getName()){};
        SingleOutputStreamOperator<PackageBean> cod = transformedWorkers.process(new ProcessFunction<PackageBean, PackageBean>() {
            @Override
            public void processElement(PackageBean brokerBean, Context context, Collector<PackageBean> collector) throws Exception {
                if (brokerBean.getTarget().contains(DevicePrefixName.Center.getName())) {
                    context.output(outputTag, brokerBean);
                } else {
                    collector.collect(brokerBean);
                }
            }
        }).setParallelism(1);
        DataStream<PackageBean> center = cod.getSideOutput(outputTag);
        //TODO: Send To The True Cod and Center
        DataStreamSink ToCod = cod.addSink(new CommonDataSender<>("localhost", ExecutorParameters.fromWorkerToCodPort ,maxNumRetries,retryInterval)).setParallelism(1);
        DataStreamSink ToCenter = center.addSink(new CommonDataSender<>("localhost", ExecutorParameters.fromWorkerToCenterPort, maxNumRetries,retryInterval)).setParallelism(1);
    }
    public DataStream<PackageBean> measureWorkerLayerRecord(DataStream<PackageBean> stream, int workerNum) {
        DataStream<PackageBean> inputMap = stream.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                beginProcess(packageBean.getId(), JSON.toJSONString(packageBean).length() * 2);
                logger.debug("Worker Layer received data: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);


        // 按照target keyBy到多个输出流（worker)
        KeyedStream<PackageBean, Integer> workers = PhysicalPartition.partitionByTarget(DevicePrefixName.Worker.getName(), workerNum,inputMap);


        // 处理逻辑
        DataStream<PackageBean> transformedWorkers = transform(workers);
        return transformedWorkers.map(new MapFunction<PackageBean, PackageBean>() {
            @Override
            public PackageBean map(PackageBean packageBean) throws Exception {
                finishProcess(packageBean.getId(), DockerRuntimeData.getNodeIdByName(packageBean.getTarget()));
                logger.debug("Worker Layer send data: {}", packageBean);
                return packageBean;
            }
        }).setParallelism(1);
    }
}

