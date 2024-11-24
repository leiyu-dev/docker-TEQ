package node.commontest;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataSender;

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
        input.addSink(new CommonDataSender<>(DockerRuntimeData.getNetworkHostNodeName(), 9000+getNodeID(), 1000000, 1000));
//        input.map(new RichMapFunction<String, Void>() {
//            private transient ServerSocket serverSocket;
//            private transient Socket clientSocket;
//            private transient PrintWriter out;
//
//            @Override
//            public void open(Configuration parameters) throws Exception {
//                super.open(parameters);
//                serverSocket = new ServerSocket(9000);
//                System.out.println("Server started, waiting for client...");
//                clientSocket = serverSocket.accept();
//                out = new PrintWriter(clientSocket.getOutputStream(), true);
//                System.out.println("Client connected.");
//            }
//
//            @Override
//            public Void map(String value) throws Exception {
//                value = "Node " + getNodeID() + ": " + value;
//                out.println(value);
//                System.out.println("Sent: " + value);
//                return null;
//            }
//
//            @Override
//            public void close() throws Exception {
//                super.close();
//                if (out != null) out.close();
//                if (clientSocket != null) clientSocket.close();
//                if (serverSocket != null) serverSocket.close();
//            }
//        });
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
