package visualizer;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.InvocationBuilder;
import node.commontest.NetworkHostNode;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.node.DefaultDockerNode;
import org.teq.simulator.Simulator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class DockerMetricsTest {
    public static void main(String[] args) {
        Simulator simulator = new Simulator(new NetworkHostNode());
        simulator.addNode(new RunningNode());
        try {
            simulator.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String containerName = DockerRuntimeData.getNodeNameList().get(1);
        DockerRunner dockerRunner = simulator.getDockerRunner();
        DockerClient dockerClient = dockerRunner.getDockerClient();
        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        dockerClient.statsCmd(containerName).exec(callback);
        while(true) {
            try {
                Statistics stats = null;
                Thread.sleep(1000);
                callback.onNext(stats);
                System.out.println(stats.getCpuStats().getCpuUsage().getTotalUsage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    static public Statistics getNextStatistics(DockerClient dockerClient, String containerId) {
        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        dockerClient.statsCmd(containerId).exec(callback);
        Statistics stats = null;
        try {
            stats = callback.awaitResult();
            callback.close();
        } catch (RuntimeException | IOException e) {
            // you may want to throw an exception here
        }
        return stats; // this may be null or invalid if the container has terminated
    }
}
