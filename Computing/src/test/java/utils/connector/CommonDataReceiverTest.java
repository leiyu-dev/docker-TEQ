package utils.connector;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.MetricsPackageBean;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CommonDataReceiverTest {
    private static final int TEST_PORT = 19999;
    private static final int ALTERNATIVE_PORT = 20000;
    
    public static void main(String[] args) throws Exception {
        System.out.println("开始测试 CommonDataReceiver 端口关闭功能...");
        
        
        // 测试1: 正常启动和关闭
        testNormalStartAndStop();
    }
    
    /**
     * 测试正常启动和关闭流程
     */
    private static void testNormalStartAndStop() throws Exception {
        System.out.println("\n=== 测试1: 正常启动和关闭 ===");

        Thread receiverThread = new Thread(() -> {
            CommonDataReceiver<MetricsPackageBean> receiver = 
            new CommonDataReceiver<>(TEST_PORT, MetricsPackageBean.class);
            try {
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                env.setParallelism(1);
                
                var stream = env.addSource(receiver)
                    .returns(TypeInformation.of(MetricsPackageBean.class))
                    .map(value -> {
                        System.out.println("Received data: " + value);
                        return value;
                    });
                env.execute("CommonDataReceiver Test");
            } catch (Exception e) {
                e.printStackTrace();    
            }
        });

        receiverThread.start();
        
        // 在另一个线程中发送测试数据
        Thread senderThread = new Thread(() -> {
            try {
                Thread.sleep(5000); // 等待接收器启动
                sendTestData();
                Thread.sleep(2000); // 发送数据后等待
                System.out.println("发送数据完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        senderThread.start();
        senderThread.join();

        System.out.println("测试1完成 - 正常启动和关闭");

        Thread senderThread2 = new Thread(() -> {
            try {
                sendTestData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        senderThread2.start();
        senderThread2.join();
    }

    
    /**
     * 发送测试数据到默认端口
     */
    private static void sendTestData() {
        sendTestDataToPort(TEST_PORT);
    }
    
    /**
     * 发送测试数据到指定端口
     */
    private static void sendTestDataToPort(int port) {
        try (Socket socket = new Socket("localhost", port);
             PrintWriter writer = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            
            MetricsPackageBean testBean = new MetricsPackageBean(
                "testSender", "testReceiver", port, "测试数据", InfoType.Data);
            
            String jsonData = com.alibaba.fastjson.JSON.toJSONString(testBean);
            
            for (int i = 0; i < 3; i++) {
                writer.println(jsonData);
                System.out.println("发送测试数据到端口 " + port + ": " + jsonData);
                Thread.sleep(500);
            }
            
            socket.close();
        } catch (Exception e) {
            System.out.println("发送数据时的预期异常: " + e.getMessage());
        }
    }
} 