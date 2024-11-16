package org.teq.simulator;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import org.teq.configurator.DockerConfigurator;
import org.teq.configurator.SimulatorConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DockerRunner {
    private static final Logger logger = LogManager.getLogger(DockerRunner.class);
    private DockerClient dockerClient;
    private String imageName;
    private void deleteAllContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true) // show all containers (not just running ones)
                .exec();

        for (Container container : containers) {
            if (container.getNames()[0].startsWith("/"+SimulatorConfigurator.classNamePrefix)) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                System.out.println("Deleted container: " + container.getId());
            }
        }
    }
    public void initRunnerWithPort(String imageName, String HostPort){
        logger.info("Initializing the docker runner with port" + HostPort);
        this.imageName = imageName;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(HostPort)  // 使用 TCP 连接
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        try{
            dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion();
        }catch (InterruptedException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        logger.info("Docker image " + imageName + " found successfully");
        this.dockerClient = dockerClient;
        deleteAllContainers();
    }

    public void initRunnerWithDefaultHost(String imageName){
        logger.info("Initializing the docker runner with default host");
        this.imageName = imageName;
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        try{
            dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion();
        }catch (InterruptedException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        logger.info("Docker image " + imageName + " found successfully");
        this.dockerClient = dockerClient;
        deleteAllContainers();
    }

    public void runContainer(String containerName){
        Volume volume = new Volume(DockerConfigurator.volumePath);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(DockerConfigurator.hostPath, volume));  // 本地文件夹路径
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)  // 传递 HostConfig（包含挂载信息）
                .withCmd("bash", "-c", DockerConfigurator.volumePath + "/" + DockerConfigurator.startScript)  // 容器启动命令
                .withName(containerName)  // 容器名称
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        if(DockerConfigurator.getStdout){
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId())
                    .withStdOut(true)  // 获取标准输出
                    .withStdErr(true)  // 获取标准错误
                    .withFollowStream(true); // 实时获取输出流
            try {
                logContainerCmd.exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        System.out.println(new String(frame.getPayload()).trim()); // 打印每条日志
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
//                throw new RuntimeException(e);
            }
        }
    }

    public void waitUntilContainerStopped(){
//        List<Container> containers = dockerClient.listContainersCmd()
//                .withShowAll(true) // show all containers (not just running ones)
//                .exec();
//        for (Container container : containers) {
//            if (container.getNames()[0].startsWith("/"+SimulatorConfigurator.classNamePrefix)) {
//                System.out.println("Waiting for container " + container.getNames()[0] + " to stop");
//                try {
//                    dockerClient.waitContainerCmd(container.getId()).exec( new ResultCallback.Adapter<WaitResponse>() {
//                        @Override
//                        public void onComplete() {
//                            System.out.println("Container " + container.getNames()[0] + " stopped");
//                        }
//                    }).awaitCompletion();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException(e);
//                }
//            }
//        }
    }
}
