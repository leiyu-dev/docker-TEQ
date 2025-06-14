package presetlayers.simpletest;

import org.teq.layer.Layer;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;
import org.teq.visualizer.SocketDisplayer;

public class PresetSimpleTest {
    public static void main(String[] args) throws Exception {

        DockerNodeParameters param = new DockerNodeParameters();
        param.setCpuUsageRate(0.5);
        EndDeviceNode end = new EndDeviceNode();
        end.setParameters(param);
        CoordinatorNode coor = new CoordinatorNode();
        coor.setParameters(param);
        WorkerNode work = new WorkerNode();
        work.setParameters(param);
        DataCenterNode dat = new DataCenterNode();
        dat.setParameters(param);
        Layer endDeviceLayer = new Layer(end,4, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(coor,1, "CoordinatorLayer");
        Layer workerLayer = new Layer(work,2, "WorkerLayer");
        Layer dataCenterLayer = new Layer(dat,1, "DataCenterLayer");

        Simulator simulator = new Simulator(new NetworkHostNode());
        simulator.addLayer(endDeviceLayer);
        simulator.addLayer(coordinatorLayer);
        simulator.addLayer(workerLayer);
        simulator.addLayer(dataCenterLayer);

        simulator.start();

        SocketDisplayer fileDisplayer = new SocketDisplayer();
        MetricsTransformer transformer = new MetricsTransformer(simulator, fileDisplayer);
        transformer.beginTransform();
        fileDisplayer.display();
    }
}
