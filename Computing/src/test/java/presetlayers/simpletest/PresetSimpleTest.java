package presetlayers.simpletest;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.teq.layer.Layer;
import org.teq.simulator.Simulator;
import org.teq.utils.connector.CommonDataReceiver;

public class PresetSimpleTest {
    public static void main(String[] args) throws Exception {
        Layer endDeviceLayer = new Layer(new EndDeviceLayer(),4, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(new CoordinatorLayer(),1, "CoordinatorLayer");
        Layer workerLayer = new Layer(new WorkerLayer(),2, "WorkerLayer");
        Layer dataCenterLayer = new Layer(new DataCenterLayer(),1, "DataCenterLayer");

        Simulator simulator = new Simulator(new NetworkHostNode());
        simulator.addLayer(endDeviceLayer);
        simulator.addLayer(coordinatorLayer);
        simulator.addLayer(workerLayer);
        simulator.addLayer(dataCenterLayer);

        simulator.start();
        var env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.addSource(new CommonDataReceiver<>( 8888,String.class)).returns(TypeInformation.of(String.class));
        stream.print();
        env.execute();
    }
}
