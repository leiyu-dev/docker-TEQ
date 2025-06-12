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
import java.net.SocketException;
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
    private int maxReconnectAttempts = 10;
    private long reconnectDelay = 5000; // 重连延迟，单位毫秒
    
    public CommonDataReceiver(int port, Class<T> typeClass) {
        this.port = port;
        this.typeClass = typeClass;
    }
    
    public CommonDataReceiver(int port, Class<T> typeClass, int maxReconnectAttempts, long reconnectDelay) {
        this.port = port;
        this.typeClass = typeClass;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.reconnectDelay = reconnectDelay;
    }

    public void open() throws IOException {
        if (receiver == null || receiver.isClosed()) {
            receiver = new ServerSocket(port);
            logger.info("receiver is listening on port " + port);
        }
        
        sender = receiver.accept();
        sender.setKeepAlive(true);
        logger.info("get sender " + sender.toString() + " connect");
        
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.warn("Error closing previous bufferedReader: " + e.getMessage());
            }
        }
        
        bufferedReader = new BufferedReader(new InputStreamReader(sender.getInputStream(), StandardCharsets.UTF_8));
        logger.info("bufferedReader: " + bufferedReader.toString());
        logger.info("sender: " + sender.toString());
        logger.info("receiver: " + receiver.toString());
    }
    
    private void closeConnection() {
        try {
            if (bufferedReader != null) {
                logger.info("close bufferedReader");
                bufferedReader.close();
                bufferedReader = null;
            }
            if (sender != null) {
                logger.info("close sender");
                sender.close();
                sender = null;
            }
        } catch (IOException e) {
            logger.error("Error closing connection: " + e.getMessage());
        }
    }
    
    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        open();
        int reconnectAttempts = 0;
        
        while (isRunning) {
            try {
                String readline;
                while (isRunning && (readline = bufferedReader.readLine()) != null) {
                    logger.trace("receive data: " + readline);
                    ctx.collect(JSON.parseObject(readline, typeClass));
                    reconnectAttempts = 0; // 重置重连次数
                }
                
                // 如果执行到这里，说明连接已断开
                logger.warn("Client connection closed. Attempting to reconnect...");
                closeConnection();
                
                // 尝试重连
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++;
                    logger.info("Reconnect attempt " + reconnectAttempts + " of " + maxReconnectAttempts);
                    
                    // 等待一段时间再重连
                    Thread.sleep(reconnectDelay);
                    
                    if (isRunning) {
                        open();
                    }
                } else {
                    logger.error("Max reconnect attempts reached. Giving up.");
                    break;
                }
            } catch (SocketException e) {
                // 处理套接字异常，通常是连接被关闭
                logger.warn("Socket exception: " + e.getMessage());
                closeConnection();
                
                if (reconnectAttempts < maxReconnectAttempts && isRunning) {
                    reconnectAttempts++;
                    logger.info("Reconnect attempt " + reconnectAttempts + " of " + maxReconnectAttempts);
                    Thread.sleep(reconnectDelay);
                    open();
                } else if (reconnectAttempts >= maxReconnectAttempts) {
                    logger.error("Max reconnect attempts reached. Giving up.");
                    break;
                }
            } catch (Exception e) {
                logger.error("Receiver error: " + e.getMessage());
                e.printStackTrace();
                
                if (reconnectAttempts < maxReconnectAttempts && isRunning) {
                    reconnectAttempts++;
                    logger.info("Reconnect attempt " + reconnectAttempts + " of " + maxReconnectAttempts);
                    closeConnection();
                    Thread.sleep(reconnectDelay);
                    open();
                } else {
                    logger.error("Max reconnect attempts reached or service stopped. Giving up.");
                    break;
                }
            }
        }
        
        cancel();
    }
    
    @Override
    public void cancel() {
        logger.info("bufferedReader: " + bufferedReader);
        logger.info("sender: " + sender);
        logger.info("receiver: " + receiver);
        logger.info("cancel receiver");
        try {
            isRunning = false;
            closeConnection();
            if (receiver != null) {
                logger.info("close receiver");
                receiver.close();
                receiver = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
