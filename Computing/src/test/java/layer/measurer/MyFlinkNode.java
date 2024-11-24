package layer.measurer;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.bouncycastle.util.Pack;
import org.teq.layer.mearsurer.MeasuredFlinkNode;
import org.teq.layer.mearsurer.PackageBean;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRuntimeData;
import org.teq.utils.connector.CommonDataSender;

public class MyFlinkNode extends MeasuredFlinkNode {

    public MyFlinkNode(){
        super();
    }
    public MyFlinkNode(DockerNodeParameters parameters) {
        super(parameters);
    }
    @Override
    public void dataProcess() {
        StreamExecutionEnvironment env = getEnv();
        String filePath = "./file.txt";
        DataStream<String> input = env.readTextFile(filePath);
        var output = input.map(new MapFunction<String, PackageBean>() {
            int count = 0;
            @Override
            public PackageBean map(String value) throws Exception {
                beginProcess(count, value.length());
                Thread.sleep(500);
                return new PackageBean(count++, value);
            }
        }).map(new MapFunction<PackageBean,String>(){
            @Override
            public String map(PackageBean value) throws Exception {
                finishProcess(value.getId());
                return value.getObject().toString();
            }
        });
        output.print();
//        output.addSink(new CommonDataSender<>(DockerRuntimeData.getNetworkHostNodeName(), 9000+getNodeID(), 1000000, 1000));
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
