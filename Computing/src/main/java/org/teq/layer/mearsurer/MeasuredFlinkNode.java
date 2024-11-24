package org.teq.layer.mearsurer;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SocketClientSink;
import org.teq.configurator.NetworkConfigurator;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.docker.DockerRuntimeData;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Map;

public abstract class MeasuredFlinkNode extends AbstractFlinkNode {

    abstract public void dataProcess();
    Map<Long,BuiltInMetrics> metricsMap;
    // call this when finish every data process (usually means finish an object processing)
    public void beginProcess(long dataId,int packageLength){
        BuiltInMetrics metrics = new BuiltInMetrics();
        metrics.setTimestampIn(System.nanoTime());
        metrics.setId(dataId);
        //FIXME: memory and cpu usage record is not accurate, need to find a better way to get memory usage
        metrics.setMemoryUsage(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
        metrics.setCpuUsage(((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemLoadAverage());
        metrics.setPackageLength(packageLength);
        metricsMap.put(dataId,metrics);
    }
    public void finishProcess(int dataId){
        BuiltInMetrics metrics = metricsMap.get(dataId);
        metrics.setTimestampOut(System.nanoTime());
        StreamExecutionEnvironment env = getEnv();
        DataStream<String> metricsDataStream = env.fromElements(metrics.toString());
        metricsDataStream.addSink(new SocketClientSink<>(DockerRuntimeData.getNetworkHostNodeName()
                ,NetworkConfigurator.metricsPortBegin + getNodeID(), new SimpleStringSchema(), 0));

    }
    public void flinkProcess(){
        dataProcess();
    }
}
