package org.teq.simulator.network.connector;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
public class CommonDataReceiver implements SourceFunction<String> {
    private final String hostname;
    private final int port;
    private volatile boolean running = true;

    public CommonDataReceiver(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        while (running) {
            try (Socket socket = new Socket(hostname, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && running) {
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(line);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to connect to server, retrying in 5 seconds...");
                Thread.sleep(5000); // 等待 5 秒后重试
            }
        }
    }

    @Override
    public void cancel() {
        running = false;
    }
}
