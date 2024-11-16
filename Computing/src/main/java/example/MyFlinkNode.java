package example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
public class MyFlinkNode extends AbstractFlinkNode{
    @Override
    public void flink_process() {
        StreamExecutionEnvironment env = getEnv();
        String filePath = "./file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        input.print();
        input.writeToSocket("localhost", 9000 + getNodeID(), new SimpleStringSchema());
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
