package node.commontest;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DockerNodeParameters;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.CommonDataSender;

public class MyFlinkNode extends AbstractFlinkNode{

    public MyFlinkNode(){
        super();
    }
    public MyFlinkNode(DockerNodeParameters parameters) {
        super(parameters);
    }
    @Override
    public void flinkProcess() {
        StreamExecutionEnvironment env = getEnv();
        String filePath = "./file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        input.print();
        input.addSink(new CommonDataSender<>(DockerRuntimeData.getNetworkHostNodeName(), 9000+getNodeID(), 1000000, 1000,true));
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
