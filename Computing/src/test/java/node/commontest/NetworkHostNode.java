package node.commontest;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;
import org.teq.utils.connector.flink.javasocket.CommonDataSender;

public class NetworkHostNode extends AbstractNetworkHostNode {
    @Override
    public void dataProcess() {
        StreamExecutionEnvironment env = getEnv();
        List<DataStream<String>>streams = new ArrayList<>();
        DockerRuntimeData data = new DockerRuntimeData();
        List<String>nodeList = data.getNodeNameList();
        int nodeCount = nodeList.size()-1;
        for(String nodeName : nodeList){
            System.out.println(nodeName);
        }
        for(int i=1;i<=nodeCount;i++){
            DataStream<String> stream = env.addSource(new CommonDataReceiver<>(9000 + i, String.class)).returns(TypeInformation.of(String.class));
            streams.add(stream);
        }
        DataStream<String> mergedStream = streams.get(0);
        for(int i=1;i<nodeCount;i++){
            mergedStream = mergedStream.union(streams.get(i));
        }
        DataStreamSink sink = mergedStream.addSink(new CommonDataSender<>(data.getHostIp(),9999,10000,1000,true));
    }
}
