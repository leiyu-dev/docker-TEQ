package node.commontest;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;

public class NetworkHostNode extends AbstractFlinkNode{
    @Override
    public void flinkProcess() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<String>>streams = new ArrayList<>();
        DockerRuntimeData data = new DockerRuntimeData();
        List<String>nodeList = data.getNodeNameList();
        int nodeCount = nodeList.size()-1;
        for(String nodeName : nodeList){
            System.out.println(nodeName);
        }
        for(int i=1;i<=nodeCount;i++){
            DataStream<String> stream = env.addSource(new CommonDataReceiver<>(9000 + i, String.class)).returns(TypeInformation.of(String.class));
            streams.add(stream);
        }
        DataStream<String> mergedStream = streams.get(0);
        for(int i=1;i<nodeCount;i++){
            mergedStream = mergedStream.union(streams.get(i));
        }
        DataStreamSink sink = mergedStream.addSink(new CommonDataSender<>(data.getHostIp(),8888,10000,1000));


//        mergedStream.map(new s<String, Void>() {
//            private transient ServerSocket serverSocket;
//            private transient Socket clientSocket;
//            private transient PrintWriter out;
//
//            @Override
//            public void open(Configuration parameters) throws Exception {
//                super.open(parameters);
//                serverSocket = new ServerSocket(8888,50, InetAddress.getByName("0.0.0.0"));
//                System.out.println("Server started, waiting for client...");
//                clientSocket = serverSocket.accept();
//                out = new PrintWriter(clientSocket.getOutputStream(), true);
//                System.out.println("Client connected.");
//            }
//
//            @Override
//            public Void map(String value) throws Exception {
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
    }
}
