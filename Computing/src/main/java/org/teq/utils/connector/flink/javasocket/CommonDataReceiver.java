package org.teq.utils.connector.flink.javasocket;

import com.alibaba.fastjson.JSON;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 这个类使用于stand-alone的环境
 * 作为服务器端Websocket ，接收来自其他层级的数据，发送给数据源
 * need to add .returns(TypeInformation.of(T.type)) or it will cause Exception in Flink
 */
public class CommonDataReceiver<T> implements SourceFunction<T> {
    private static final Logger logger = LogManager.getLogger(CommonDataReceiver.class);
    private final Class<T> typeClass;
    private int port;
    private ServerSocket receiver;
    private Socket sender;
    private BufferedReader bufferedReader;
    private boolean isRunning = true;
    public CommonDataReceiver(int port,Class<T>typeClass){
        this.port = port;
        this.typeClass = typeClass;
    }

    public void open() throws IOException {
        ServerSocket receiver = new ServerSocket(port);
        logger.info("receiver is listening on port " + port);
        this.receiver = receiver;
        sender = receiver.accept();
        sender.setKeepAlive(true);
        logger.info("get sender " + sender.toString() + " connect");
        bufferedReader = new BufferedReader(new InputStreamReader(sender.getInputStream(), StandardCharsets.UTF_8));
    }
    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        open();
        try {
            while (isRunning) {
                String readline;
                while ((readline = bufferedReader.readLine()) != null) {
                    logger.trace("receive data: " + readline);
                    ctx.collect(JSON.parseObject(readline, typeClass));
                }
            }
        }
        catch (Exception e){
            logger.error("receiver error: " + e.getMessage());
            e.printStackTrace();
            cancel();
            System.exit(0);
        }
    }
    @Override
    public void cancel() {
        try {
            isRunning = false;
            sender.close();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
