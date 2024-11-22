package node.commontest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

// 客户端线程类
class ListenThread implements Runnable {
    private final String host;
    private final int port;

    public ListenThread(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket();) {
            socket.connect(new InetSocketAddress(host, port), 5000); // 设置连接超时 5000ms
            var input = socket.getInputStream();
            socket.setSoTimeout(5000); // 设置读取超时 5000ms
            System.out.println("Connected to server at " + host + ":" + port);

            byte[] buffer = new byte[1024]; // 缓冲区
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) { // 读取数据
                String message = new String(buffer, 0, bytesRead); // 转换为字符串
                System.out.println("Received: " + message);
            }

        } catch (Exception ex) {
            System.err.println("Error in client thread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}