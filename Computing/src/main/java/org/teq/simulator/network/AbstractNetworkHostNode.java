package org.teq.simulator.network;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.SimulatorConfig;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.node.AbstractFlinkNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.CommonDataSender;
import org.teq.utils.connector.flink.netty.HighPerformanceDataReceiver;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNetworkHostNode extends AbstractFlinkNode {
    @Override
    public void flinkProcess() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<BuiltInMetrics>> streams = new ArrayList<>();
        List<String>nodeList = DockerRuntimeData.getNodeNameList();
        for(String nodeName : nodeList){
            System.out.println(nodeName);
        }
        DataStream<BuiltInMetrics> stream = env.addSource(new HighPerformanceDataReceiver<>(SimulatorConfig.metricsPort , BuiltInMetrics.class)).returns(TypeInformation.of(BuiltInMetrics.class));
        DataStreamSink sink = stream.addSink(new CommonDataSender<>(DockerRuntimeData.getHostIp(),SimulatorConfig.MetricsReceiverPort,10000,1000,true)).setParallelism(1);
        dataProcess();
    }
    abstract public void dataProcess();
}
