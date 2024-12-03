package org.teq.node;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
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

    /**
     * Override this method to set up your own environment
     */
    static public void initEnvironment(){
        Configuration conf = new Configuration();
        conf.setInteger(RestOptions.PORT,7000);
        env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);
    }

    static public void startEnvironment(){
        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
