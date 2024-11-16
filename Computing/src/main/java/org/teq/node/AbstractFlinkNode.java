package org.teq.node;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public abstract class AbstractFlinkNode extends AbstractDockerNode {
    abstract public void flink_process();

    @Override
    public void process(){
        initEnvironment();
        flink_process();
        startEnvironment();
    }
    
    private StreamExecutionEnvironment env;

    public StreamExecutionEnvironment getEnv() {
        return env;
    }

    public void initEnvironment(){
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
    }

    public void startEnvironment(){
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
