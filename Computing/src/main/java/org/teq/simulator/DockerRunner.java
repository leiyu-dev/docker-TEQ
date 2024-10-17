package org.teq.simulator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.teq.configurator.DockerConfigurator;

public class DockerRunner {
    private DockerClient dockerClient;
    private String imageName;
    public void InitRunnerWithTcpHost(String imageName,int tcpPort){
        this.imageName = imageName;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:" + tcpPort)  // 使用 TCP 连接
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        dockerClient.pullImageCmd(imageName).start();
        this.dockerClient = dockerClient;
    }

    public void InitRunnerWithNpipeHost(String imageName){
        this.imageName = imageName;
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        dockerClient.pullImageCmd(imageName).start();
    }

    public void RunContainer(String containerName){
        Volume volume = new Volume(DockerConfigurator.volumePath);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(DockerConfigurator.hostPath, volume));  // 本地文件夹路径
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)  // 传递 HostConfig（包含挂载信息）
                .withCmd("bash", "-c", "cd /var/lib/teq && bash run.sh")
                .withName(containerName)  // 容器名称
                .exec();
    }
}
