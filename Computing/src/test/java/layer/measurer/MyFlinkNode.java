package layer.measurer;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.MeasuredFlinkNode;
import org.teq.mearsurer.MetricsPackageBean;
import org.teq.node.DockerNodeParameters;

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
        var output = input.map(new MapFunction<String, MetricsPackageBean>() {
            int count = 0;
            @Override
            public MetricsPackageBean map(String value) throws Exception {
                var packageBean = new MetricsPackageBean(value);
                beginProcess(packageBean.getId());
                Thread.sleep(500);
                return packageBean;
            }
        }).map(new MapFunction<MetricsPackageBean,String>(){
            @Override
            public String map(MetricsPackageBean value) throws Exception {
                String result = value.getObject().toString();
                finishProcess(value.getId(),0,result.length(), InfoType.Data);
                return value.getObject().toString();
            }
        });
        output.print();
//        output.addSink(new CommonDataSender<>(DockerRuntimeData.getNetworkHostNodeName(), 9000+getNodeID(), 1000000, 1000));
        System.out.println("Hello World from " + this.getClass().getName());
    }
}
