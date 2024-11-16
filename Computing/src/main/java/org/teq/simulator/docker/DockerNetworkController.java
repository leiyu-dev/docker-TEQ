package org.teq.simulator.docker;

import org.teq.configurator.DockerConfigurator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;


public class DockerNetworkController {
    private DockerClient dockerClient;
    private String networkHostName;
    DockerNetworkController(DockerClient client) {
        this.dockerClient = client;
    }
    void createNetworkHostContainer(String imageName,String networkHostName) {
        this.networkHostName = networkHostName;
        Ports portBindings = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(8888);
        portBindings.bind(exposedPort, Ports.Binding.bindPort(8888)); // bind the port 8888 to the container
        Volume volume = new Volume(DockerConfigurator.volumePath);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(DockerConfigurator.hostPath, volume))  // 本地文件夹路径
                .withPortBindings(portBindings);
        String[] command = {
            "bash", "-c",
            "chmod -R 777 " + DockerConfigurator.volumePath + "&& bash "+ DockerConfigurator.volumePath + "/" + DockerConfigurator.startScriptName
        };
        
        // create a network host container
        CreateContainerResponse container =  dockerClient.createContainerCmd(imageName)
                .withCmd(command)
                .withName(networkHostName)
                .withHostConfig(hostConfig)
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
    }
}
