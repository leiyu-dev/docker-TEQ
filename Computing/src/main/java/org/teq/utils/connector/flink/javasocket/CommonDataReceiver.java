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
    private volatile ServerSocket receiver;
    private volatile Socket sender;
    private volatile BufferedReader bufferedReader;
    private volatile boolean isRunning = true;
    
    public CommonDataReceiver(int port, Class<T> typeClass) {
        this.port = port;
        this.typeClass = typeClass;
    }

    public void open() throws IOException {
        logger.info("Attempting to open receiver on port {}", port);
        
        // Try to create ServerSocket with retry mechanism for port conflicts
        int maxRetries = 5;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ServerSocket receiver = new ServerSocket(port);
                receiver.setReuseAddress(true); // Allow address reuse
                logger.info("Receiver is listening on port {}", port);
                this.receiver = receiver;
                break;
            } catch (IOException e) {
                if (attempt < maxRetries) {
                    logger.warn("Failed to bind to port {} (attempt {}), retrying in {} ms: {}", 
                               port, attempt, retryDelay, e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted while waiting to retry port binding", ie);
                    }
                } else {
                    logger.error("Failed to bind to port {} after {} attempts", port, maxRetries);
                    throw e;
                }
            }
        }
        
        // Accept connection
        logger.info("Waiting for client connection on port {}", port);
        sender = receiver.accept();
        sender.setKeepAlive(true);
        sender.setSoTimeout(1000); // Set socket timeout for graceful shutdown
        logger.info("Client connected: {}", sender.toString());
        bufferedReader = new BufferedReader(new InputStreamReader(sender.getInputStream(), StandardCharsets.UTF_8));
    }
    
    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        try {
            open();
            
            while (isRunning) {
                try {
                    String readline = bufferedReader.readLine();
                    if (readline != null) {
                        logger.trace("Received data: {}", readline);
                        T data = JSON.parseObject(readline, typeClass);
                        ctx.collect(data);
                    } else {
                        // Connection closed by client
                        if (isRunning) {
                            logger.info("Client disconnected from port {}", port);
                            break;
                        }
                    }
                } catch (SocketException e) {
                    if (isRunning) {
                        logger.warn("Socket exception while reading data: {}", e.getMessage());
                        break;
                    } else {
                        // Expected during shutdown
                        logger.debug("Socket closed during shutdown");
                        break;
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        logger.error("IO error while reading data: {}", e.getMessage());
                        break;
                    } else {
                        // Expected during shutdown
                        logger.debug("IO error during shutdown: {}", e.getMessage());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in CommonDataReceiver for port {}: {}", port, e.getMessage(), e);
            throw e;
        } finally {
            // Ensure cleanup even if an exception occurs
            cleanup();
        }
    }
    
    @Override
    public void cancel() {
        logger.info("Cancelling CommonDataReceiver on port {}", port);
        isRunning = false;
        cleanup();
    }
    
    /**
     * Clean up resources safely
     */
    private void cleanup() {
        // Close BufferedReader
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
                logger.debug("Closed BufferedReader for port {}", port);
            } catch (IOException e) {
                logger.warn("Error closing BufferedReader for port {}: {}", port, e.getMessage());
            } finally {
                bufferedReader = null;
            }
        }
        
        // Close client Socket
        if (sender != null) {
            try {
                sender.close();
                logger.debug("Closed client socket for port {}", port);
            } catch (IOException e) {
                logger.warn("Error closing client socket for port {}: {}", port, e.getMessage());
            } finally {
                sender = null;
            }
        }
        
        // Close ServerSocket
        if (receiver != null) {
            try {
                receiver.close();
                logger.info("Closed server socket for port {}", port);
            } catch (IOException e) {
                logger.warn("Error closing server socket for port {}: {}", port, e.getMessage());
            } finally {
                receiver = null;
            }
        }
        
        logger.info("CommonDataReceiver cleanup completed for port {}", port);
    }
}
