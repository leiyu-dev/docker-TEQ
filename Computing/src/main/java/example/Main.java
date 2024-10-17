package example;

import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.Simulator;

import java.lang.reflect.Method;
import java.net.URL;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineFormat;
import java.net.URLClassLoader;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
class mynode extends AbstractFlinkNode{
    public void process(){
        initEnvironment();
        StreamExecutionEnvironment env = getEnv();
        String filePath = "path/to/your/file.txt";
        FileSource<String> fileSource = FileSource
        .forRecordStreamFormat(new TextLineFormat(), filePath)
        .build();

        // 配置 WatermarkStrategy（此处没有使用 Watermark）
        WatermarkStrategy<String> noWatermarks = WatermarkStrategy.noWatermarks();

        // 通过 fromSource() 使用自定义 Source，读取文件内容并打印
        env.fromSource(fileSource, noWatermarks, "File Source")
            .print();
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        Simulator simulator = new Simulator();
        simulator.addNode(mynode.class);
        simulator.start();
//        simulator.addNode(new AbstractFlinkNode(){
//            @Override
//            public void process(){
//                System.out.println("I DO PROCESS 2");
//                //do other thing
//            }
//        });
//        simulator.start();
    }
}