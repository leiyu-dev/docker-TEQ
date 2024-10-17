package example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.Simulator;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
class mynode extends AbstractFlinkNode{
    public void process(){
        initEnvironment();
        StreamExecutionEnvironment env = getEnv();
        String filePath = "path/to/your/file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        input.print();
        startEnvironment();
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator();
        simulator.addNode(mynode.class);
        simulator.start();
//        simulator.addNode(new AbstractFlinkNode(){
//            @Override
//            public void process(){
//                System.out.println("I DO PROCESS 2");
//                //do other thing
//            }
//        });
//        simulator.start();
    }
}