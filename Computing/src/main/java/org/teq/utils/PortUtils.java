package org.teq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * port utility class
 * provide port availability check and wait function
 */
public class PortUtils {
    
    private static final Logger logger = LogManager.getLogger(PortUtils.class);
    
    /**
     * check if the port is available
     * 
     * @param port the port to check
     * @return true if the port is available, false if the port is occupied
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
     * check if the port is in use
     * 
     * @param host the host name
     * @param port the port number
     * @return true if the port is in use, false if the port is not in use
     */
    public static boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * wait for the port to become available
     * 
     * @param port the port to wait for
     * @param maxWaitTimeMs the maximum wait time (milliseconds)
     * @param checkIntervalMs the check interval (milliseconds)
     * @return true if the port becomes available within the specified time, false if timeout
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
     * wait for the port to become available (using default parameters)
     * 
     * @param port the port to wait for
     * @return true if the port becomes available within 10 seconds, false if timeout
     */
    public static boolean waitForPortAvailable(int port) {
        return waitForPortAvailable(port, 10000, 500); // 10 seconds maximum wait time, 500 milliseconds check interval
    }
} 