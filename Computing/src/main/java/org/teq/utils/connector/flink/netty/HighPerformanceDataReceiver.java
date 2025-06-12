package org.teq.utils.connector.flink.netty;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 高性能数据接收器，使用Netty处理大量并发客户端连接
 * 可以处理超过200个客户端的并发连接
 * 需要添加 .returns(TypeInformation.of(T.type)) 否则会导致 Flink 异常
 */
public class HighPerformanceDataReceiver<T> implements SourceFunction<T> {
    private static final Logger logger = LogManager.getLogger(HighPerformanceDataReceiver.class);
    
    private final Class<T> typeClass;
    private final int port;
    private volatile boolean isRunning = true;
    
    // Netty 相关组件
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    // 数据队列，用于在Netty线程和Flink线程之间传递数据
    private final BlockingQueue<T> dataQueue = new ArrayBlockingQueue<>(10000);
    
    // 连接计数器
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    
    // 配置参数
    private static final int BOSS_THREADS = 1;
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_FRAME_LENGTH = 8192;
    
    public HighPerformanceDataReceiver(int port, Class<T> typeClass) {
        this.port = port;
        this.typeClass = typeClass;
    }

    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        // 初始化Netty服务器
        initNettyServer();
        
        logger.info("High performance receiver is listening on port " + port);
        
        // 主循环：从队列中获取数据并发送给Flink
        while (isRunning) {
            try {
                T data = dataQueue.poll(1, TimeUnit.SECONDS);
                if (data != null) {
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(data);
                    }
                }
            } catch (InterruptedException e) {
                if (isRunning) {
                    logger.warn("Data polling interrupted", e);
                }
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing data", e);
            }
        }
        
        // 清理资源
        cleanup();
    }
    
    /**
     * 初始化Netty服务器
     */
    private void initNettyServer() throws InterruptedException {
        // 创建线程组
        bossGroup = new NioEventLoopGroup(BOSS_THREADS, 
            new DefaultThreadFactory("netty-boss"));
        workerGroup = new NioEventLoopGroup(WORKER_THREADS, 
            new DefaultThreadFactory("netty-worker"));
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                    .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 添加分隔符解码器（按行分割）
                            pipeline.addLast(new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH, 
                                Delimiters.lineDelimiter()));
                            
                            // 添加字符串解码器
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            
                            // 添加业务处理器
                            pipeline.addLast(new DataHandler());
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            
        } catch (Exception e) {
            cleanup();
            throw e;
        }
    }
    
    /**
     * 数据处理器
     */
    private class DataHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            int count = connectionCount.incrementAndGet();
            logger.info("New client connected: {}, total connections: {}", 
                ctx.channel().remoteAddress(), count);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            int count = connectionCount.decrementAndGet();
            logger.info("Client disconnected: {}, remaining connections: {}", 
                ctx.channel().remoteAddress(), count);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                String jsonString = (String) msg;
                if (jsonString != null && !jsonString.trim().isEmpty()) {
                    logger.trace("Received data from {}: {}", 
                        ctx.channel().remoteAddress(), jsonString);
                    
                    T data = JSON.parseObject(jsonString, typeClass);
                    
                    // 将数据放入队列，如果队列满了则丢弃最旧的数据
                    if (!dataQueue.offer(data)) {
                        dataQueue.poll(); // 移除最旧的数据
                        dataQueue.offer(data); // 插入新数据
                        logger.warn("Data queue is full, dropped oldest data");
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing data from {}: {}", 
                    ctx.channel().remoteAddress(), e.getMessage(), e);
            }
        }
        

        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Exception in channel {}: {}", 
                ctx.channel().remoteAddress(), cause.getMessage(), cause);
            ctx.close();
        }
    }

    @Override
    public void cancel() {
        logger.info("Cancelling high performance data receiver...");
        isRunning = false;
        cleanup();
        logger.info("High performance data receiver has been cancelled.");
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        // 关闭服务器通道
        if (serverChannel != null && serverChannel.isActive()) {
            try {
                serverChannel.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while closing server channel", e);
            }
        }
        
        // 关闭线程组
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
        }
        
        logger.info("Netty resources cleaned up, final connection count: {}", 
            connectionCount.get());
    }
} 