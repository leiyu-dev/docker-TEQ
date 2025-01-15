package org.teq.simulator.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import javassist.bytecode.analysis.ControlFlow;
import org.teq.configurator.SimulatorConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.node.DockerNodeParameters;
import com.github.dockerjava.api.model.Network.Ipam;
import com.github.dockerjava.api.model.Network.Ipam.Config;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DockerRunner {
    private static final Logger logger = LogManager.getLogger(DockerRunner.class);
    private DockerClient dockerClient;

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    private String hostPort;
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
    public DockerRunner(String imageName, String hostPort){
        logger.info("Initializing the docker runner with port" + hostPort);
        this.imageName = imageName;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(hostPort)  // 使用 TCP 连接
                .build();
        this.hostPort = hostPort;
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
        this.hostPort = DefaultDockerClientConfig.createDefaultConfigBuilder().build().getDockerHost().toString();
//        System.exit(0);
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
        if(parameters.getCpuRestrictType() == DockerNodeParameters.CpuRestrictType.ROUGH){
            hostConfig = hostConfig.withCpuPeriod(1000000L)
                    .withCpuQuota((long) (1000000 * parameters.getCpuUsageRate()));  // 最大使用 100% 的一个 CPU
        }
        else { // precise
            System.out.println("Precise CPU restriction is not supported yet");
            System.exit(-1);
        }
        hostConfig = hostConfig.withOomKillDisable(true); //disable out of memory kill


        /* Memory restriction */
        hostConfig = hostConfig.withMemory((long) parameters.getMemorySize() * 1024 * 1024 * 1024)
                .withMemorySwap((long) parameters.getMemorySize() * 1024 * 1024 * 1024);

        /* Network restriction */
        hostConfig = hostConfig
                .withPrivileged(true)
                .withCapAdd(Capability.NET_ADMIN)
                .withSysctls(Collections.singletonMap("net.ipv6.conf.all.disable_ipv6", "0"));



        String[] command = {
            "bash", "-c",
            "chmod -R 777 " + SimulatorConfigurator.volumePath + " && " +
            "tc qdisc add dev eth0 root handle 1: htb default 1 && " +
            "tc class add dev eth0 parent 1: classid 1:1 htb rate " + parameters.getNetworkOutBandwidth() + "kbps ceil " + parameters.getNetworkOutBandwidth() + "kbps && " +
            "tc qdisc add dev eth0 parent 1:1 handle 10: netem delay "+ parameters.getNetworkOutLatency() +"ms && " +
            "tc class add dev eth0 parent 1: classid 1:2 htb rate 100mbit ceil 100mbit && " +
            "tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip tos 0x10 0xff flowid 1:2 && " +
            "bash "+ SimulatorConfigurator.volumePath + "/" + SimulatorConfigurator.startScriptName
        };
        String[] env = {
            "NODE_ID=" + containerId,
            "NODE_NAME=" + containerName,
            "IN_DOCKER=1",
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


    //deprecated: have bug
    public void beginDockerMetricsCollection(List<BlockingQueue<Double>>cpuQueueList, List<BlockingQueue<Double>>memoryQueueList){
        Thread getterThread =  new Thread(()->{
            List<String>nodeList = DockerRuntimeData.getNodeNameList();
            for(int i = 0; i < nodeList.size(); i++){
                String containerName = nodeList.get(i);
                StatsCmd statsCmd = dockerClient.statsCmd(containerName);
                int finalI = i;
                statsCmd.exec(new ResultCallback<Statistics>() {
                    @Override
                    public void close() throws IOException {}
                    private Statistics lastStat = null; // 独立的 lastStat
                    @Override
                    public void onStart(Closeable closeable) {}

                    @Override
                    public void onNext(Statistics stats) {
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        if (lastStat == null) {
                            lastStat = stats; // 初始化上一次数据
                            return;
                        }

                        // 计算 CPU 使用率
                        double cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage()
                                - lastStat.getCpuStats().getCpuUsage().getTotalUsage();
                        double systemCpuDelta = stats.getCpuStats().getSystemCpuUsage()
                                - lastStat.getCpuStats().getSystemCpuUsage();
                        double cpuUsage = (cpuDelta / systemCpuDelta) * stats.getCpuStats().getOnlineCpus();

                        // 获取内存使用情况
                        long memoryUsageRaw = stats.getMemoryStats().getUsage();
                        double memoryUsage = memoryUsageRaw / 1024 / 1024 / 1024.0;

                        try {
                            BlockingQueue<Double> cpuQueue = cpuQueueList.get(finalI);
                            BlockingQueue<Double> memoryQueue = memoryQueueList.get(finalI);
                            cpuQueue.put(cpuUsage * 100);
                            memoryQueue.put(memoryUsage);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        lastStat = stats; // 更新上一次的统计数据
                    }

                    @Override
                    public void onError(Throwable throwable) {}

                    @Override
                    public void onComplete() {}
                });
                }
        });
        getterThread.start();
    }
    public void beginInspectNode(String containerName, BlockingQueue<Double>cpuQueue, BlockingQueue<Double>memoryQueue, BlockingQueue<Double>cpuTimeQueue, BlockingQueue<Double>memoryTimeQueue){
        Thread getterThread =  new Thread(()->{
            long startTime = utils.getStartTime();
            StatsCmd statsCmd = dockerClient.statsCmd(containerName);
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void close() throws IOException {}
                private Statistics lastStat = null; // 独立的 lastStat
                @Override
                public void onStart(Closeable closeable) {}

                @Override
                public void onNext(Statistics stats) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (lastStat == null) {
                        lastStat = stats; // 初始化上一次数据
                        return;
                    }

                    // 计算 CPU 使用率
                    double cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage()
                            - lastStat.getCpuStats().getCpuUsage().getTotalUsage();
                    double systemCpuDelta = stats.getCpuStats().getSystemCpuUsage()
                            - lastStat.getCpuStats().getSystemCpuUsage();
                    double cpuUsage = (cpuDelta / systemCpuDelta) * stats.getCpuStats().getOnlineCpus();

                    // 获取内存使用情况
                    long memoryUsageRaw = stats.getMemoryStats().getUsage();
                    double memoryUsage = memoryUsageRaw / 1024 / 1024 / 1024.0;

                    try {
                        cpuQueue.put(cpuUsage * 100);
                        memoryQueue.put(memoryUsage * 1024.0);
                        long currentTime = System.currentTimeMillis();
                        cpuTimeQueue.put( Math.round((currentTime - startTime) / 1000.0 * 10.0) / 10.0);
                        memoryTimeQueue.put( Math.round((currentTime - startTime) / 1000.0 * 10.0) / 10.0);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    lastStat = stats; // 更新上一次的统计数据
                }

                @Override
                public void onError(Throwable throwable) {}

                @Override
                public void onComplete() {}
            });
        });
        getterThread.start();
    }


    //TODO: use better ways
    public void beginDockerMetricsCollectionTry(List<BlockingQueue<Double>>cpuQueueList, List<BlockingQueue<Double>>memoryQueueList) throws Exception {
        Thread getterThread =  new Thread(()-> {
            List<String> nodeList = DockerRuntimeData.getNodeNameList();
            while(true) {
                for (int i = 0; i < nodeList.size(); i++) {
                    String containerName = nodeList.get(i);
                    try {
                        // 执行 docker stats 命令，过滤指定容器
                        ProcessBuilder processBuilder = new ProcessBuilder(
                                "docker", "stats", containerName, "--no-stream", "--format",
                                "{{.Container}},{{.CPUPerc}},{{.MemUsage}}"
                        );
                        Process process = processBuilder.start();

                        // 读取输出
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith(containerName + ",")) {
                                String[] stats = line.split(",");
                                String containerRead = stats[0];

                                // 0.00% -> 0.00
                                Double cpuUsage = Double.parseDouble(stats[1].split("%")[0]);

                                // 2.24GiB / 503.3GiB -> 2.24
                                Double memUsage;
                                if(stats[2].contains("GiB / "))memUsage = Double.parseDouble(stats[2].split("GiB / ")[0]) * 1000.0;
                                else if(stats[2].contains("MiB / "))memUsage = Double.parseDouble(stats[2].split("MiB")[0]);
                                else if(stats[2].contains("KiB / "))memUsage = Double.parseDouble(stats[2].split("KiB")[0]) / 1000.0;
                                else break;

                                if (containerRead.equals(containerName)) {
                                    BlockingQueue<Double> cpuQueue = cpuQueueList.get(i);
                                    BlockingQueue<Double> memoryQueue = memoryQueueList.get(i);
                                    logger.info("CPU: " + cpuUsage + "%, Memory: " + memUsage + "MB" + " for " + containerName);
                                    cpuQueue.put(cpuUsage);
                                    memoryQueue.put(memUsage);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        getterThread.start();
    }
    public void cleanUp(){
        deleteAllContainers();
    }

    public void changeCpuUsageRate(String containerName, double cpuUsageRate) throws IllegalArgumentException{
        if(cpuUsageRate < 0){
            throw new IllegalArgumentException("CPU usage rate should be greater than 0");
        }
        dockerClient.updateContainerCmd(containerName)
                .withCpuPeriod(1000000)
                .withCpuQuota( (int)(1000000 * cpuUsageRate)).exec();
        logger.info("Updated CPU usage rate for container " + containerName + " to " + cpuUsageRate);
    }
    public void changeMemorySize(String containerName, double memorySize) throws IllegalArgumentException{
        if(memorySize < 0){
            throw new IllegalArgumentException("Memory size should be greater than 0");
        }
        dockerClient.updateContainerCmd(containerName)
                .withMemory((long) memorySize * 1024 * 1024 * 1024)
                .withMemorySwap((long) memorySize * 1024 * 1024 * 1024).exec();
        logger.info("Updated memory size for container " + containerName + " to " + memorySize + " GB");
    }

    /*
String[] command = {
"tc qdisc add dev eth0 root handle 1: htb default 1 && " +
"tc class add dev eth0 parent 1: classid 1:1 htb rate " + parameters.getNetworkOutBandwidth() + "kbps ceil " + parameters.getNetworkOutBandwidth() + "kbps && " +
"tc qdisc add dev eth0 parent 1:1 handle 10: netem delay "+ parameters.getNetworkOutLatency() +"ms && " +
"tc class add dev eth0 parent 1: classid 1:2 htb rate 100mbit ceil 100mbit && " +
"tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip tos 0x10 0xff flowid 1:2 && " +
"bash "+ SimulatorConfigurator.volumePath + "/" + SimulatorConfigurator.startScriptName
};
     */
    public void changeNetworkOutBandwidth(String containerName, double outBandwidth) throws IllegalArgumentException{
        if(outBandwidth < 0){
            throw new IllegalArgumentException("Network out bandwidth should be greater than 0");
        }
        try {
            String command = String.format(
                    "tc qdisc replace dev eth0 root handle 1: htb default 10 && " +
                            "tc class replace dev eth0 parent 1: classid 1:1 htb rate %.2fkbps ceil %.2fkbps",
                    outBandwidth, outBandwidth
            );

            dockerClient.execCreateCmd(containerName)
                    .withCmd("bash", "-c", command)
                    .exec();
            dockerClient.execStartCmd(containerName).exec(new ExecStartResultCallback()).awaitCompletion();
            logger.info("Updated network out bandwidth for container " + containerName + " to " + outBandwidth + " kbps");
        } catch (Exception e) {
            logger.error("Failed to update network out bandwidth for container " + containerName, e);
        }
    }

    public void changeNetworkOutLatency(String containerName, double latency) throws IllegalArgumentException{
        if(latency < 0){
            throw new IllegalArgumentException("Network out latency should be greater than 0");
        }
        try {
            String command = String.format(
                    "tc qdisc replace dev eth0 root netem delay %.2fms",
                    latency
            );

            dockerClient.execCreateCmd(containerName)
                    .withCmd("bash", "-c", command)
                    .exec();
            dockerClient.execStartCmd(containerName).exec(new ExecStartResultCallback()).awaitCompletion();
            logger.info("Updated network out latency for container " + containerName + " to " + latency + " ms");
        } catch (Exception e) {
            logger.error("Failed to update network out latency for container " + containerName, e);
        }
    }

}
