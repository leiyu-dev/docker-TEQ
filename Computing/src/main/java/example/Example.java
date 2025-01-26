package example;

import example.Coordinator;
import example.DataCenter;
import example.EndDevice;
import org.teq.layer.Layer;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.Simulator;
import presetlayers.Network;

public class Example {
    public static void main(String[] args) {
        DockerNodeParameters param = new DockerNodeParameters();
        param.setCpuUsageRate(0.5);
        EndDevice endDevice = new EndDevice();
        endDevice.setParameters(param);
        Coordinator coordinator = new Coordinator();
        coordinator.setParameters(param);
        Worker worker = new Worker();
        worker.setParameters(param);
        DataCenter dataCenter = new DataCenter();
        dataCenter.setParameters(param);
        Layer endDeviceLayer = new Layer(endDevice,10, "EndDeviceLayer");
        Layer coordinatorLayer = new Layer(coordinator,1, "CoordinatorLayer");
        Layer workerLayer = new Layer(worker,2, "WorkerLayer");
        Layer dataCenterLayer = new Layer(dataCenter,1, "DataCenterLayer");

        Simulator simulator = new Simulator(new Network());
        simulator.addLayer(endDeviceLayer);
        simulator.addLayer(coordinatorLayer);
        simulator.addLayer(workerLayer);
        simulator.addLayer(dataCenterLayer);

        try {
            simulator.start();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
