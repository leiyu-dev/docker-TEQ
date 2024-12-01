package org.teq.simulator.network;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.layer.mearsurer.BuiltInMetrics;
import org.teq.node.AbstractFlinkNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNetworkHostNode extends AbstractFlinkNode {
    @Override
    public void flinkProcess() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<BuiltInMetrics>> streams = new ArrayList<>();
        List<String>nodeList = DockerRuntimeData.getNodeNameList();
        int nodeCount = nodeList.size()-1;
        for(String nodeName : nodeList){
            System.out.println(nodeName);
        }
        for(int i=1;i<=nodeCount;i++){
            DataStream<BuiltInMetrics> stream = env.addSource(new CommonDataReceiver<>(SimulatorConfigurator.metricsPortBegin + i, BuiltInMetrics.class))
                    .returns(TypeInformation.of(BuiltInMetrics.class));
            streams.add(stream);
        }
        DataStream<BuiltInMetrics> mergedStream = streams.get(0);
        for(int i=1;i<nodeCount;i++){
            mergedStream = mergedStream.union(streams.get(i));
        }
        DataStreamSink sink = mergedStream.addSink(new CommonDataSender<>(DockerRuntimeData.getHostIp(),SimulatorConfigurator.MetricsReceiverPort,10000,1000));
        dataProcess();
    }
    abstract public void dataProcess();
}
