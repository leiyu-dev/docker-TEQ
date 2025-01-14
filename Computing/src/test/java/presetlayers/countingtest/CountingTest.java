package presetlayers.countingtest;

import org.teq.layer.Layer;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;
import org.teq.visualizer.SocketDisplayer;
public class CountingTest {
    public static void main(String[] args) {
        DockerNodeParameters param = new DockerNodeParameters();
        param.setCpuUsageRate(0.5);
        EndDevice end = new EndDevice();
        end.setParameters(param);
        Coordinator coor = new Coordinator();
        coor.setParameters(param);
        Worker work = new Worker();
        work.setParameters(param);
        DataCenter dat = new DataCenter();
        dat.setParameters(param);
        Layer endDeviceLayer = new Layer(end,4, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(coor,1, "CoordinatorLayer");
        Layer workerLayer = new Layer(work,2, "WorkerLayer");
        Layer dataCenterLayer = new Layer(dat,1, "DataCenterLayer");

        Simulator simulator = new Simulator(new Network());
        simulator.addLayer(endDeviceLayer);
        simulator.addLayer(coordinatorLayer);
        simulator.addLayer(workerLayer);
        simulator.addLayer(dataCenterLayer);

        try {
            simulator.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SocketDisplayer fileDisplayer = new SocketDisplayer();
        MetricsTransformer transformer = new MetricsTransformer(simulator, fileDisplayer);
        try {
            transformer.beginTransform();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fileDisplayer.display();
    }
}
