package org.teq.simulator.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.teq.configurator.SimulatorConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.node.DockerNodeParameters;
import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import org.teq.utils.utils;

import java.util.List;

public class DockerRunner {
    private static final Logger logger = LogManager.getLogger(DockerRunner.class);
    private DockerClient dockerClient;
    private String imageName;
    private String networkHostName;
    private void deleteAllContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true) // show all containers (not just running ones)
                .exec();

        for (Container container : containers) {
            if (container.getNames()[0].startsWith("/"+ SimulatorConfigurator.classNamePrefix)) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                System.out.println("Deleted container: " + container.getId());
            }
        }
    }
    private void deleteNetwork(){
        var networks = dockerClient.listNetworksCmd().exec();
        String networkId = networks.stream()
                .filter(network -> network.getName().equals(SimulatorConfigurator.networkName))
                .map(network -> network.getId())
                .findFirst()
                .orElse(null);
        if (networkId == null)return;
        dockerClient.removeNetworkCmd(networkId)
                .exec();
    }
    public DockerNetworkController dockerNetworkController;

    private void initDockerRunner(){
        List<Image>images = dockerClient.listImagesCmd().exec();
        boolean imageExists = images.stream()
                .flatMap(image -> image.getRepoTags() != null ? List.of(image.getRepoTags()).stream() : List.of().stream())
                .anyMatch(repoTag -> imageName.equals(repoTag));
        if(!imageExists) {
            try {
                dockerClient.pullImageCmd(imageName)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        logger.info("Docker image " + imageName + " found successfully");
        deleteAllContainers();
        deleteNetwork();
        dockerNetworkController = new DockerNetworkController(dockerClient);
        CreateNetworkResponse network = dockerClient.createNetworkCmd()
                .withName(SimulatorConfigurator.networkName)
                .withDriver("bridge") // 使用桥接网络
                .withIpam(new Ipam().withConfig(new Config().withSubnet(SimulatorConfigurator.networkSubnet).withGateway(SimulatorConfigurator.networkGateway)))
                .exec();
        Network teqNetwork = dockerClient.inspectNetworkCmd().withNetworkId(network.getId()).exec();
        String gateway = teqNetwork.getIpam().getConfig().get(0).getGateway();
        utils.writeStringToFile(SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.hostIpFileName, gateway);
        logger.info("Gateway: " + gateway);
        logger.info("Create network" + network.getId());
    }


    /* 
    * This method is used to initialize the docker runner with a specific host port
    * @param imageName the name of the docker image
    * @param HostPort the host port
    */
    public DockerRunner(String imageName, String HostPort){
        logger.info("Initializing the docker runner with port" + HostPort);
        this.imageName = imageName;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(HostPort)  // 使用 TCP 连接
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        this.dockerClient = dockerClient;
        initDockerRunner();
    }

    /*
    * This method is used to initialize the docker runner with default host
    * @param imageName the name of the docker image
    */
    public DockerRunner(String imageName){
        logger.info("Initializing the docker runner with default host");
        this.imageName = imageName;
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        this.dockerClient = dockerClient;
        initDockerRunner();
    }

    public void runNetworkHostContainer(String containerName, int containerId){
        networkHostName = containerName;
        dockerNetworkController.createNetworkHostContainer(imageName, containerName, containerId);
    }

    public void runContainer(String containerName, int containerId, DockerNodeParameters parameters){
        logger.info("Running container " + containerName);
        Volume volume = new Volume(SimulatorConfigurator.volumePath);
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withBinds(new Bind(SimulatorConfigurator.hostPath, volume))  // 本地文件夹路径
                .withNetworkMode(SimulatorConfigurator.networkName);

        /* CPU restriction */
        if(parameters.cpuRestrictType == DockerNodeParameters.CpuRestrictType.ROUGH){
            hostConfig = hostConfig.withCpuPeriod(1000000L)
                    .withCpuQuota((long) (1000000 * parameters.cpuUsageRate));  // 最大使用 100% 的一个 CPU
        }
        else { // precise
            System.out.println("Precise CPU restriction is not supported yet");
            System.exit(-1);
        }
        hostConfig = hostConfig.withOomKillDisable(true); //disable out of memory kill


        /* Memory restriction */
        hostConfig = hostConfig.withMemory((long) parameters.memorySize * 1024 * 1024 * 1024)
                .withMemorySwap((long) parameters.memorySize * 1024 * 1024 * 1024);



        String[] command = {
            "bash", "-c",
            "chmod -R 777 " + SimulatorConfigurator.volumePath + "&& bash "+ SimulatorConfigurator.volumePath + "/" + SimulatorConfigurator.startScriptName
        };
        String[] env = {
            "NODE_ID=" + containerId,
            "NODE_NAME=" + containerName,
        };
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)  // 传递 HostConfig（包含挂载信息）
                .withName(containerName)  // 容器名称
                .withCmd(command)  // 容器启动命令
                .withEnv(env)
                .withHostName(containerName)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        if(SimulatorConfigurator.getStdout){
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
        logger.info("Container " + containerName + " started successfully");
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

    public void cleanUp(){
        deleteAllContainers();
    }
}
