package org.teq.backend;

import javassist.bytecode.analysis.ControlFlow;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// 捕获System.out日志并通过WebSocket广播
public class LogHandler {
    public static BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    public static void redirectConsoleOutput() {
        PrintStream consoleStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                logQueue.add(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                logQueue.add(new String(b, off, len));
            }
        });
        System.setOut(consoleStream);
        System.setErr(consoleStream);
    }
}
