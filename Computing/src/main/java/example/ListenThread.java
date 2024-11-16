package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ListenThread implements Runnable {
    private static final Logger logger = LogManager.getLogger(ListenThread.class);
    private final int port = 8888;
    
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port + "...");

            // 无限循环监听端口
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client: " + clientSocket.getInetAddress());

                // 读取客户端发送的数据
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received: " + line);
                    }
                }

                clientSocket.close();
                System.out.println("Client disconnected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
