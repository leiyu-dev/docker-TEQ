package layer.measurer;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.layer.mearsurer.BuiltInMetrics;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.utils.connector.CommonDataSender;

import java.util.ArrayList;
import java.util.List;

public class NetworkHostNode extends AbstractNetworkHostNode {
    @Override
    public void dataProcess() {
    }
}
