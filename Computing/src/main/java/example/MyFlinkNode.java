package example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
public class MyFlinkNode extends AbstractFlinkNode{
    @Override
    public void process() {
        initEnvironment();
        StreamExecutionEnvironment env = getEnv();
        String filePath = "./file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        input.print();
        startEnvironment();
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
