package org.teq.node;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.operators.source.TimestampsAndWatermarks;

public abstract class AbstractFlinkNode implements FlinkNode {
    private StreamExecutionEnvironment env;

    public StreamExecutionEnvironment getEnv() {
        return env;
    }

    public void initEnvironment(){
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
    }

    public void startEnvironment() throws Exception {
        env.execute();
    }
}
