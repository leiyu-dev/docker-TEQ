package org.teq.utils.connector.flink.javasocket;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.alibaba.fastjson.JSON;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.SerializableObject;
import org.teq.mearsurer.MetricsPackageBean;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to send data to port that property of target is specified in MetricsPackageBean
 * @param <T> MetricsPackageBean
 * @warning without target property, this class will throw exception
 */
public class TargetedDataSender<T extends MetricsPackageBean> extends RichSinkFunction<T> {
    private static final Logger logger = LogManager.getLogger(TargetedDataSender.class);
    private Map<Pair<String,Integer>,Pair<Socket,BufferedWriter>> senderMap = new ConcurrentHashMap<>();
    private int maxNumRetries;
    private int retryInterval;

    private final SerializableObject lock = new SerializableObject();
    public TargetedDataSender(int maxNumRetries, int retryInterval){//retry interval in milliseconds
        this.maxNumRetries = maxNumRetries;
        this.retryInterval = retryInterval;
    }

    private void createConnection(String hostName, int port) throws IOException, InterruptedException {
        Thread.sleep(retryInterval);
        if(senderMap.containsKey(Pair.of(hostName,port)))return;
        Socket client = new Socket();
        logger.info("Trying to connect to socket server at " + hostName + ":" + port);
        client.connect(new InetSocketAddress(hostName, port));
        client.setKeepAlive(true);
        client.setTcpNoDelay(true);
        logger.info("Connected to socket server at " + hostName + ":" + port);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
        senderMap.put(Pair.of(hostName,port),Pair.of(client,bufferedWriter));
    }
    private void retryConnection(String hostName, int port, int maxNumRetries){
        try{
            if(senderMap.get(Pair.of(hostName,port)) != null || maxNumRetries == 0)return;
            synchronized (lock) {
                createConnection(hostName,port);
            }
        }
        catch (Exception e){
            retryConnection(hostName, port, maxNumRetries - 1);
        }
    }
    private Pair<Socket,BufferedWriter> getConnection(String hostName, int port){
        if(senderMap.containsKey(Pair.of(hostName,port))) return senderMap.get(Pair.of(hostName,port));
        try {
            synchronized (lock) {
                createConnection(hostName,port);
            }
        }
        catch (Exception e) {
            retryConnection(hostName, port, maxNumRetries);
        }
        return senderMap.get(Pair.of(hostName,port));
    }
    @Override
    public void invoke(T value, SinkFunction.Context context){
        String msg = JSON.toJSONString(value);
        var senderPair = getConnection(value.getTarget(),value.getTargetPort());
        BufferedWriter bufferedWriter = senderPair.getRight();
        logger.debug("Sending message to " + value.getTarget() + ":" + value.getTargetPort() + " : " + msg);
        try {
            bufferedWriter.write(msg + "\n");
            bufferedWriter.flush();
        }
        catch (IOException e) {
            logger.error("Failed to send message to " + value.getTarget() + ":" + value.getTargetPort() + " : " + msg);
            System.exit(0);
            e.printStackTrace();
        }
    }
    @Override
    public void close() throws Exception {
        for(var senderPair : senderMap.entrySet()){
            Pair<Socket,BufferedWriter> sender = senderPair.getValue();
            BufferedWriter bufferedWriter = sender.getRight();
            Socket socket = sender.getLeft();
            if(bufferedWriter != null)bufferedWriter.close();
            if(socket != null)socket.close();
        }
    }
}
