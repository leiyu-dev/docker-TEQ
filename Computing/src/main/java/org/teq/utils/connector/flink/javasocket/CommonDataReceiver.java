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
 * CommonDataReceiver is a generic Flink source function that acts as a TCP server
 * to receive JSON data from external clients and convert it to typed objects.
 * 
 * This class is designed for stand-alone environments and provides robust 
 * connection handling with automatic reconnection capabilities.
 * 
 * Key features:
 * - Acts as a TCP server listening on a specified port
 * - Automatically deserializes JSON strings to specified type T
 * - Handles connection failures with configurable retry logic
 * - Provides clean resource management and connection cleanup
 * 
 * Usage: Remember to add .returns(TypeInformation.of(T.class)) when using this
 * source function to avoid type erasure issues in Flink.
 * 
 * @param <T> The type of objects this receiver will produce
 */
public class CommonDataReceiver<T> implements SourceFunction<T> {
    
    /** Logger instance for this class */
    private static final Logger logger = LogManager.getLogger(CommonDataReceiver.class);
    
    /** The class type for JSON deserialization */
    private final Class<T> typeClass;
    
    /** The port number on which the server socket will listen */
    private int port;
    
    /** The server socket that accepts incoming connections */
    private ServerSocket receiver;
    
    /** The client socket connection */
    private Socket sender;
    
    /** Buffered reader for reading data from the client socket */
    private BufferedReader bufferedReader;
    
    /** Flag to control the running state of the receiver */
    private boolean isRunning = true;
    
    /** Maximum number of reconnection attempts before giving up */
    private int maxReconnectAttempts = 10;
    
    /** Delay between reconnection attempts in milliseconds */
    private long reconnectDelay = 5000; // Reconnection delay in milliseconds
    
    /**
     * Creates a new CommonDataReceiver with default reconnection settings.
     * 
     * @param port The port number to listen on
     * @param typeClass The class type for JSON deserialization
     */
    public CommonDataReceiver(int port, Class<T> typeClass) {
        this.port = port;
        this.typeClass = typeClass;
    }
    
    /**
     * Creates a new CommonDataReceiver with custom reconnection settings.
     * 
     * @param port The port number to listen on
     * @param typeClass The class type for JSON deserialization
     * @param maxReconnectAttempts Maximum number of reconnection attempts
     * @param reconnectDelay Delay between reconnection attempts in milliseconds
     */
    public CommonDataReceiver(int port, Class<T> typeClass, int maxReconnectAttempts, long reconnectDelay) {
        this.port = port;
        this.typeClass = typeClass;
        this.maxReconnectAttempts = maxReconnectAttempts;
        this.reconnectDelay = reconnectDelay;
    }

    /**
     * Opens the server socket and waits for a client connection.
     * This method initializes the server socket, accepts a client connection,
     * and sets up the buffered reader for data reception.
     * 
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public void open() throws IOException {
        // Create server socket if it doesn't exist or is closed
        if (receiver == null || receiver.isClosed()) {
            receiver = new ServerSocket(port);
            logger.info("receiver is listening on port " + port);
        }
        
        // Accept incoming client connection
        sender = receiver.accept();
        sender.setKeepAlive(true); // Enable TCP keep-alive
        logger.info("get sender " + sender.toString() + " connect");
        
        // Close previous buffered reader if exists
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.warn("Error closing previous bufferedReader: " + e.getMessage());
            }
        }
        
        // Create new buffered reader for the accepted connection
        bufferedReader = new BufferedReader(new InputStreamReader(sender.getInputStream(), StandardCharsets.UTF_8));
        logger.info("bufferedReader: " + bufferedReader.toString());
        logger.info("sender: " + sender.toString());
        logger.info("receiver: " + receiver.toString());
    }
    
    /**
     * Closes the current client connection and cleans up associated resources.
     * This method safely closes the buffered reader and client socket.
     */
    private void closeConnection() {
        try {
            // Close buffered reader
            if (bufferedReader != null) {
                logger.info("close bufferedReader");
                bufferedReader.close();
                bufferedReader = null;
            }
            // Close client socket
            if (sender != null) {
                logger.info("close sender");
                sender.close();
                sender = null;
            }
        } catch (IOException e) {
            logger.error("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Main execution method that runs the data receiving loop.
     * This method implements the SourceFunction interface and handles:
     * - Continuous data reception from connected clients
     * - JSON deserialization and data emission to Flink
     * - Connection failure detection and automatic reconnection
     * - Graceful error handling and resource cleanup
     * 
     * @param ctx The source context for emitting data to Flink
     * @throws Exception if an unrecoverable error occurs
     */
    @Override
    public void run(SourceContext<T> ctx) throws Exception {
        open(); // Initialize connection
        int reconnectAttempts = 0;
        
        // Main processing loop
        while (isRunning) {
            try {
                String readline;
                // Read data from client until connection is closed or service is stopped
                while (isRunning && (readline = bufferedReader.readLine()) != null) {
                    logger.trace("receive data: " + readline);
                    // Deserialize JSON and emit to Flink
                    ctx.collect(JSON.parseObject(readline, typeClass));
                    reconnectAttempts = 0; // Reset reconnection attempts on successful read
                }
                
                // If execution reaches here, the connection is closed
                logger.warn("Client connection closed. Attempting to reconnect...");
                closeConnection();
                
                // Attempt to reconnect if within retry limits
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++;
                    logger.info("Reconnect attempt " + reconnectAttempts + " of " + maxReconnectAttempts);
                    
                    // Wait for a while before reconnecting
                    Thread.sleep(reconnectDelay);
                    
                    // Reopen connection if service is still running
                    if (isRunning) {
                        open();
                    }
                } else {
                    logger.error("Max reconnect attempts reached. Giving up.");
                    break;
                }
            } catch (SocketException e) {
                // Handle socket exceptions, usually connection is closed
                logger.warn("Socket exception: " + e.getMessage());
                closeConnection();
                
                // Attempt reconnection if within limits and service is running
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
                // Handle all other exceptions
                logger.error("Receiver error: " + e.getMessage());
                e.printStackTrace();
                
                // Attempt reconnection if within limits and service is running
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
        
        // Clean up resources before exiting
        cancel();
    }
    
    /**
     * Cancels the source function and closes all resources.
     * This method is called by Flink when the job is cancelled or stopped.
     * It ensures proper cleanup of all network resources.
     */
    @Override
    public void cancel() {
        logger.info("bufferedReader: " + bufferedReader);
        logger.info("sender: " + sender);
        logger.info("receiver: " + receiver);
        logger.info("cancel receiver");
        try {
            // Stop the main processing loop
            isRunning = false;
            
            // Close client connection
            closeConnection();
            
            // Close server socket
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
