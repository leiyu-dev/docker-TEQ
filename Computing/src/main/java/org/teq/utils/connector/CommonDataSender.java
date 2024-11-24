package org.teq.utils.connector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.alibaba.fastjson.JSON;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.SerializableObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 这个类使用于stand-alone的环境
 * 建立websocket的客户端，这里为了提升鲁棒性，添加了间断时间重复尝试连接的机制。
 */
public class CommonDataSender<T> extends RichSinkFunction<T> {
    private static final Logger logger = LogManager.getLogger(CommonDataSender.class);
    private String hostName;
    private int port;
    private final SerializableObject lock = new SerializableObject();
    private transient Socket client;
    protected BufferedWriter bufferedWriter;
    private int maxNumRetries;
    private int retryInterval;
    public CommonDataSender(String hostName, int port, int maxNumRetries, int retryInterval){//retry interval in milliseconds
        this.hostName = hostName;
        this.port = port;
        this.maxNumRetries = maxNumRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        try {
            synchronized (lock) {
                createConnection();
            }
        }
        catch (IOException e) {
            retryConnection(maxNumRetries);
            //throw new IOException("Cannot connect to socket server at " + hostName + ":" + port, e);
        }
    }
    private void createConnection() throws IOException, InterruptedException {
        Thread.sleep(retryInterval);
        client = new Socket();
        logger.info("Trying to connect to socket server at " + hostName + ":" + port);
        client.connect(new InetSocketAddress(hostName, port));
        client.setKeepAlive(true);
        client.setTcpNoDelay(true);
        logger.info("Connected to socket server at " + hostName + ":" + port);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
    }
    private void retryConnection(int maxNumRetries){
        try{
            if(client.isConnected() || maxNumRetries == 0)
                return;
            createConnection();
        }
        catch (Exception e){
            retryConnection(maxNumRetries - 1);
        }
    }

    @Override
    public void invoke(T value, SinkFunction.Context context){
        String msg = JSON.toJSONString(value);
        try {
            bufferedWriter.write(msg + "\n");
            bufferedWriter.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void close() throws Exception {
        synchronized (lock) {
            lock.notifyAll();
            try {
                if (bufferedWriter!= null) {
                    bufferedWriter.close();
                }
            }
            finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }

}
