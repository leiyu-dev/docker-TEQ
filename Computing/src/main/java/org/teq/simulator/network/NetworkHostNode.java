package org.teq.simulator.network;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.executiongraph.restart.RestartStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SocketClientSink;
import org.apache.flink.streaming.api.functions.source.SocketTextStreamFunction;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.network.connector.CommonDataReceiver;

public class NetworkHostNode extends AbstractFlinkNode{
    @Override
    public void flink_process() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<String>>streams = new ArrayList<>();
        for(int i=1;i<=100;i++){
            DataStream<String> stream = env.addSource(new CommonDataReceiver(null, 9000+i));
            streams.add(stream);
        }
        DataStream<String> mergedStream = streams.get(0);
        for(int i=1;i<100;i++){
            mergedStream = mergedStream.union(streams.get(i));
        }
        mergedStream.map(new RichMapFunction<String, Void>() {
            private transient ServerSocket serverSocket;
            private transient Socket clientSocket;
            private transient PrintWriter out;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);
                serverSocket = new ServerSocket(8888,50, InetAddress.getByName("0.0.0.0"));
                System.out.println("Server started, waiting for client...");
                clientSocket = serverSocket.accept();
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                System.out.println("Client connected.");
            }

            @Override
            public Void map(String value) throws Exception {
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
    }
}
