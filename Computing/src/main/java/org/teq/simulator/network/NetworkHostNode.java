package org.teq.simulator.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.teq.node.AbstractFlinkNode;

public class NetworkHostNode extends AbstractFlinkNode{
    @Override
    public void flink_process() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<String>>streams = new ArrayList<>();
        for(int i=1;i<=10;i++){
            DataStream<String> stream = env.addSource(new SocketTextStreamFunction("localhost", 9000+i, "\n", 0));
            streams.add(stream);
        }
        DataStream<String> mergedStream = streams.get(0);
        for(int i=1;i<10;i++){
            mergedStream = mergedStream.union(streams.get(i));
        }
        mergedStream.writeToSocket("localhost", 8888, new SimpleStringSchema());
    }
}
