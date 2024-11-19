package example;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.node.AbstractFlinkNode;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import javax.sql.CommonDataSource;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class MyFlinkNode extends AbstractFlinkNode{
    @Override
    public void flink_process() {
        StreamExecutionEnvironment env = getEnv();
        String filePath = "./file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        input.print();
        input.map(new RichMapFunction<String, Void>() {
            private transient ServerSocket serverSocket;
            private transient Socket clientSocket;
            private transient PrintWriter out;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                serverSocket = new ServerSocket(9000 + getNodeID());
                System.out.println("Server started, waiting for client...");
                clientSocket = serverSocket.accept();
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                System.out.println("Client connected.");
            }

            @Override
            public Void map(String value) throws Exception {
                value = "Node " + getNodeID() + ": " + value;
                out.println(value);
                System.out.println("Sent: " + value);
                return null;
            }

            @Override
            public void close() throws Exception {
                super.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
            }
        });
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
