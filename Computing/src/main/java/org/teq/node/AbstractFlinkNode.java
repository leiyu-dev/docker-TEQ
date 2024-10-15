package org.teq.node;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public abstract class AbstractFlinkNode implements FlinkNode {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
    public static void runEnvironment() throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.execute();
        System.out.println("Run Environment");
    }
}
