package org.teq.simulator.docker;

import org.teq.configurator.SimulatorConfigurator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;


public class DockerNetworkController {
    private DockerClient dockerClient;
    private String networkHostName;
    DockerNetworkController(DockerClient client) {
        this.dockerClient = client;
    }
    void createNetworkHostContainer(String imageName,String networkHostName,int containerId) {
        this.networkHostName = networkHostName;
        String[] env = {
                "NODE_ID=" + containerId,
                "NODE_NAME=" + networkHostName,
                "IN_DOCKER=1",
        };
//        ExposedPort exposedPort = ExposedPort.tcp(8888);
//        Ports.Binding hostPortBinding = Ports.Binding.bindPort(8888);
//        PortBinding portBinding = new PortBinding(hostPortBinding, exposedPort);
        Volume volume = new Volume(SimulatorConfigurator.volumePath);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(SimulatorConfigurator.hostPath, volume))  // 本地文件夹路径
                .withNetworkMode(SimulatorConfigurator.networkName);
//                .withPortBindings(portBinding);
        String[] command = {
            "bash", "-c",
            "chmod -R 777 " + SimulatorConfigurator.volumePath + "&& bash "+ SimulatorConfigurator.volumePath + "/" + SimulatorConfigurator.startScriptName
        };
        
        // create a network host container
        CreateContainerResponse container =  dockerClient.createContainerCmd(imageName)
                .withCmd(command)
                .withName(networkHostName)
//                .withExposedPorts(exposedPort)
                .withHostConfig(hostConfig)
                .withEnv(env)
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
    }
}
