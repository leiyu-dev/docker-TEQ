package org.teq.utils.connector.flink.netty;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.alibaba.fastjson.JSON;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.teq.mearsurer.MetricsPackageBean;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高性能目标数据发送器，使用Netty处理大量并发连接到不同服务器
 * 可以高效地向超过200个不同服务器发送数据
 * 
 * @param <T> MetricsPackageBean
 * @warning 没有target属性时，此类将抛出异常
 */
public class HighPerformanceTargetedDataSender<T extends MetricsPackageBean> extends RichSinkFunction<T> {
    private static final Logger logger = LogManager.getLogger(HighPerformanceTargetedDataSender.class);
    
    // Netty相关组件
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    
    // 连接管理
    private final ConcurrentHashMap<Pair<String, Integer>, Channel> channelMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Pair<String, Integer>, Boolean> connectingMap = new ConcurrentHashMap<>();
    
    // 配置参数
    private final int maxNumRetries;
    private final int retryInterval; // 毫秒
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_FRAME_LENGTH = 8192;
    
    // 统计信息
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public HighPerformanceTargetedDataSender(int maxNumRetries, int retryInterval) {
        this.maxNumRetries = maxNumRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
        super.open(parameters);
        initNettyClient();
        logger.info("HighPerformanceTargetedDataSender initialized with {} worker threads", WORKER_THREADS);
    }

    /**
     * 初始化Netty客户端
     */
    private void initNettyClient() {
        workerGroup = new NioEventLoopGroup(WORKER_THREADS, 
            new DefaultThreadFactory("netty-client-worker"));
        
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 添加字符串编码器
                        pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                        
                        // 添加连接处理器
                        pipeline.addLast(new ClientHandler());
                    }
                });
    }

    /**
     * 获取或创建到指定服务器的连接
     */
    private Channel getOrCreateConnection(String hostname, int port) {
        Pair<String, Integer> key = Pair.of(hostname, port);
        
        // 检查现有连接
        Channel existingChannel = channelMap.get(key);
        if (existingChannel != null && existingChannel.isActive()) {
            return existingChannel;
        }
        
        // 避免重复连接
        if (connectingMap.putIfAbsent(key, true) != null) {
            // 另一个线程正在连接，等待一下再检查
            try {
                Thread.sleep(100);
                Channel channel = channelMap.get(key);
                if (channel != null && channel.isActive()) {
                    return channel;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
        
        try {
            // 创建新连接
            logger.info("Creating connection to {}:{}", hostname, port);
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(hostname, port));
            
            // 异步处理连接结果
            future.addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    connectingMap.remove(key);
                    
                    if (channelFuture.isSuccess()) {
                        Channel channel = channelFuture.channel();
                        channelMap.put(key, channel);
                        totalConnections.incrementAndGet();
                        activeConnections.incrementAndGet();
                        logger.info("Successfully connected to {}:{}, total connections: {}", 
                            hostname, port, totalConnections.get());
                        
                        // 设置连接关闭监听器
                        channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                            @Override
                            public void operationComplete(ChannelFuture future) {
                                channelMap.remove(key);
                                activeConnections.decrementAndGet();
                                logger.info("Connection to {}:{} closed, active connections: {}", 
                                    hostname, port, activeConnections.get());
                            }
                        });
                    } else {
                        logger.error("Failed to connect to {}:{}: {}", 
                            hostname, port, channelFuture.cause().getMessage());
                        
                        // 安排重连
                        if (maxNumRetries > 0) {
                            scheduleReconnect(hostname, port, maxNumRetries);
                        }
                    }
                }
            });
            
                         // 同步等待连接完成（不设置超时）
             if (future.await(1000, TimeUnit.MILLISECONDS)) {
                 if (future.isSuccess()) {
                     return future.channel();
                 }
             }
            
        } catch (Exception e) {
            logger.error("Exception while connecting to {}:{}: {}", hostname, port, e.getMessage(), e);
            connectingMap.remove(key);
        }
        
        return null;
    }

    /**
     * 安排重连
     */
    private void scheduleReconnect(String hostname, int port, int retriesLeft) {
        if (retriesLeft <= 0) {
            logger.warn("Max retries reached for {}:{}", hostname, port);
            return;
        }
        
        workerGroup.schedule(() -> {
            logger.info("Retrying connection to {}:{}, retries left: {}", hostname, port, retriesLeft - 1);
            getOrCreateConnection(hostname, port);
        }, retryInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invoke(T value, SinkFunction.Context context) {
        String hostname = value.getTarget();
        int port = value.getTargetPort();
        
        if (hostname == null || hostname.trim().isEmpty()) {
            logger.error("Target hostname is null or empty");
            return;
        }
        
        Channel channel = getOrCreateConnection(hostname, port);
        if (channel == null || !channel.isActive()) {
            logger.warn("No active connection to {}:{}, message will be dropped", hostname, port);
            return;
        }
        
        String jsonMessage = JSON.toJSONString(value) + "\n";
        
        // 异步发送数据
        ChannelFuture future = channel.writeAndFlush(jsonMessage);
        future.addListener(new GenericFutureListener<ChannelFuture>() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()) {
                    logger.trace("Message sent successfully to {}:{}: {}", hostname, port, jsonMessage.trim());
                } else {
                    logger.error("Failed to send message to {}:{}: {}", 
                        hostname, port, channelFuture.cause().getMessage());
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        logger.info("Closing HighPerformanceTargetedDataSender...");
        
        // 关闭所有连接
        for (Channel channel : channelMap.values()) {
            if (channel.isActive()) {
                try {
                    channel.close().sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Interrupted while closing channel", e);
                }
            }
        }
        
        // 关闭线程组
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        logger.info("HighPerformanceTargetedDataSender closed. Final stats - Total connections: {}, Active connections: {}", 
            totalConnections.get(), activeConnections.get());
        
        super.close();
    }

    /**
     * 客户端连接处理器
     */
    private class ClientHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            logger.debug("Channel active: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            logger.debug("Channel inactive: {}", ctx.channel().remoteAddress());
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Exception in channel {}: {}", 
                ctx.channel().remoteAddress(), cause.getMessage(), cause);
            ctx.close();
        }
    }
    
    /**
     * 获取当前活跃连接数
     */
    public int getActiveConnectionCount() {
        return activeConnections.get();
    }
    
    /**
     * 获取总连接数
     */
    public int getTotalConnectionCount() {
        return totalConnections.get();
    }
} 