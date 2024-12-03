package presetlayers.simpletest;

import org.teq.layer.Layer;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;

public class PresetSimpleTest {
    public static void main(String[] args) throws Exception {

        DockerNodeParameters param = new DockerNodeParameters();
        param.cpuUsageRate = 0.5;
        EndDeviceLayer end = new EndDeviceLayer();
        end.setParameters(param);
        CoordinatorLayer coor = new CoordinatorLayer();
        coor.setParameters(param);
        WorkerLayer work = new WorkerLayer();
        work.setParameters(param);
        DataCenterLayer dat = new DataCenterLayer();
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
        MetricsTransformer transformer = new MetricsTransformer();
        transformer.beginTransform();
    }
}
