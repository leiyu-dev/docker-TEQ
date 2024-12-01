package org.teq.utils.connector;

import com.alibaba.fastjson.JSON;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 这个类使用于stand-alone的环境
 * 作为服务器端Websocket ，接收来自其他层级的数据，发送给数据源
 * 需要添加 .returns(TypeInformation.of(T.type)) 否则会导致 Flink 异常
 */
public class MultiThreadDataReceiver<T> implements SourceFunction<T> {
    private static final Logger logger = LogManager.getLogger(CommonDataReceiver.class);
    private final Class<T> typeClass;
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;

    // 使用线程池来管理客户端连接
    private transient ExecutorService executorService;

    // 维护所有活跃的客户端连接
    private final Set<Socket> clientSockets = Collections.synchronizedSet(new HashSet<>());

    public MultiThreadDataReceiver(int port, Class<T> typeClass) {
        this.port = port;
        this.typeClass = typeClass;
    }

    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        // 初始化线程池
        executorService = Executors.newCachedThreadPool();

        // 启动服务器监听
        serverSocket = new ServerSocket(port);
        logger.info("Receiver is listening on port " + port);

        // 使用一个独立的线程来接受新的客户端连接
        Thread acceptThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setKeepAlive(true);
                    clientSockets.add(clientSocket);
                    logger.info("Accepted new client: " + clientSocket.toString());

                    // 为每个客户端启动一个新的线程来处理数据
                    executorService.submit(() -> handleClient(clientSocket, ctx));
                } catch (IOException e) {
                    if (isRunning) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        });

        acceptThread.start();

        // 等待直到取消被调用
        while (isRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // 忽略中断
            }
        }

        // 关闭服务器监听
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        // 等待接受线程结束
        acceptThread.join();
    }

    /**
     * 处理单个客户端的连接
     */
    private void handleClient(Socket clientSocket, SourceContext<T> ctx) {
        try (BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {
            while (isRunning) {
                String readline;
                while((readline = bufferedReader.readLine()) != null){
                    logger.info("Received data from " + clientSocket.toString() + ": " + readline);
                    T data = JSON.parseObject(readline, typeClass);
                    // 使用 synchronized 确保线程安全
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(data);
                    }
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                logger.error("Error reading from client " + clientSocket.toString(), e);
            }
        } finally {
            System.out.println("closed connection:" + isRunning);
            // 移除并关闭客户端连接
            clientSockets.remove(clientSocket);
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
                logger.info("Closed client connection: " + clientSocket.toString());
            } catch (IOException e) {
                logger.error("Error closing client socket " + clientSocket.toString(), e);
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        // 关闭所有客户端连接
        synchronized (clientSockets) {
            for (Socket socket : clientSockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Error closing client socket " + socket.toString(), e);
                }
            }
            clientSockets.clear();
        }

        // 关闭服务器监听
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error closing server socket", e);
            }
        }

        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        logger.info("CommonDataReceiver has been cancelled.");
    }
}
