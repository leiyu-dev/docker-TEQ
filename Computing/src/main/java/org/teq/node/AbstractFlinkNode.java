package org.teq.node;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public abstract class AbstractFlinkNode extends AbstractDockerNode {
    public AbstractFlinkNode() {
        super(new DockerNodeParameters());//default parameters
    }
    public AbstractFlinkNode(DockerNodeParameters parameters) {
        super(parameters);
    }

    abstract public void flinkProcess();
    @Override
    public void process(){
        initEnvironment();
        flinkProcess();
        startEnvironment();
    }
    
    static private StreamExecutionEnvironment env;

    public static StreamExecutionEnvironment getEnv() {
        return env;
    }

    static public void initEnvironment(){
        env = StreamExecutionEnvironment.getExecutionEnvironment();
    }

    static public void startEnvironment(){
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
