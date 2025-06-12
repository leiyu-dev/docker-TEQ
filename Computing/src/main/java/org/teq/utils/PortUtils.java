package org.teq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 端口实用工具类
 * 提供端口可用性检查和等待功能
 */
public class PortUtils {
    
    private static final Logger logger = LogManager.getLogger(PortUtils.class);
    
    /**
     * 检查端口是否可用
     * 
     * @param port 要检查的端口
     * @return true 如果端口可用，false 如果端口被占用
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 检查端口是否正在被使用
     * 
     * @param host 主机名
     * @param port 端口号
     * @return true 如果端口正在被使用，false 如果端口未被使用
     */
    public static boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 等待端口变为可用
     * 
     * @param port 要等待的端口
     * @param maxWaitTimeMs 最大等待时间（毫秒）
     * @param checkIntervalMs 检查间隔（毫秒）
     * @return true 如果端口在指定时间内变为可用，false 如果超时
     */
    public static boolean waitForPortAvailable(int port, long maxWaitTimeMs, long checkIntervalMs) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + maxWaitTimeMs;
        
        logger.info("Waiting for port {} to become available (max wait: {} ms)", port, maxWaitTimeMs);
        
        while (System.currentTimeMillis() < endTime) {
            if (isPortAvailable(port)) {
                logger.info("Port {} is now available", port);
                return true;
            }
            
            try {
                Thread.sleep(checkIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for port {} to become available", port);
                return false;
            }
        }
        
        logger.warn("Timeout waiting for port {} to become available", port);
        return false;
    }
    
    /**
     * 等待端口变为可用（使用默认参数）
     * 
     * @param port 要等待的端口
     * @return true 如果端口在10秒内变为可用，false 如果超时
     */
    public static boolean waitForPortAvailable(int port) {
        return waitForPortAvailable(port, 10000, 500); // 10秒最大等待时间，500毫秒检查间隔
    }
} 