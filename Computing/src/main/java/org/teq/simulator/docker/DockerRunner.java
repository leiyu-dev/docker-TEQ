package org.teq.simulator.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.google.common.util.concurrent.AtomicDoubleArray;
import org.teq.configurator.ExecutorParameters;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DockerRunner {
    private static final Logger logger = LogManager.getLogger(DockerRunner.class);
    private DockerClient dockerClient;

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    private String hostPort;
    private String imageName;
    private String networkHostName;
    public void closeAllContainers() {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 调整线程池大小
        try {
            // 获取所有相关容器
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .withNameFilter(Collections.singletonList(SimulatorConfigurator.classNamePrefix))
                    .exec();

            List<Future<?>> futures = new ArrayList<>();
            for (Container container : containers) {
                futures.add(executorService.submit(() -> {
                    try {
                        // 检查容器是否正在运行
                        if (!"running".equals(container.getState())) {
                            logger.info("Container already stopped: " + container.getId());
                            return;
                        }
                        // 停止容器
                        dockerClient.stopContainerCmd(container.getId()).exec();
                        logger.info("Closed container: " + container.getId());
                    } catch (Exception e) {
                        logger.error("Failed to stop container: " + container.getId(), e);
                    }
                }));
            }

            // 等待所有任务完成
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            logger.error("Failed to close all containers", e);
        } finally {
            executorService.shutdown();
        }
    }



    //start a container
    public void startContainer(String containerName){
        dockerClient.startContainerCmd(containerName).exec();
        logger.info("Container " + containerName + " started successfully");
    }
    private void deleteAllContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true) // show all containers (not just running ones)
                .exec();

        for (Container container : containers) {
            if (container.getNames()[0].startsWith("/"+ SimulatorConfigurator.classNamePrefix)) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                logger.info("Deleted container: " + container.getId());
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

    public void createAndStartContainer(String containerName, int containerId, DockerNodeParameters parameters){
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
            (ExecutorParameters.useFixedLatency ? "" :
            "tc qdisc add dev eth0 root handle 1: htb default 1 && " +
            "tc class add dev eth0 parent 1: classid 1:1 htb rate " + parameters.getNetworkOutBandwidth() + "kbps ceil " + parameters.getNetworkOutBandwidth() + "kbps && " +
            "tc qdisc add dev eth0 parent 1:1 handle 10: netem delay "+ parameters.getNetworkOutLatency() +"ms && " +
            "tc class add dev eth0 parent 1: classid 1:2 htb rate 100mbit ceil 100mbit && " +
            "tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip tos 0x10 0xff flowid 1:2 && " +
            "ip link add ifb0 type ifb &&" +
            "ip link set dev ifb0 up &&" +
            "tc qdisc add dev eth0 handle ffff: ingress && " +
            "tc filter replace dev eth0 parent ffff: protocol ip u32 match u32 0 0 action mirred egress redirect dev ifb0 &&" +
            "tc qdisc replace dev ifb0 root handle 2: htb default 22 && " +
            "tc class add dev ifb0 parent 2: classid 2:22 htb rate " + parameters.getNetworkInBandwidth() + "kbps ceil " + parameters.getNetworkInBandwidth() + "kbps && " +
            "tc qdisc add dev ifb0 parent 2:22 handle 20: netem delay "+ parameters.getNetworkInLatency() +"ms && "
            ) +
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

    private AtomicBoolean shouldStop = new AtomicBoolean(false);
    private List<BlockingQueue<Double>>cpuQueueList;
    private List<BlockingQueue<Double>>memoryQueueList;
    class InspectNode{
        String containerName;
        BlockingQueue<Double>cpuQueue;
        BlockingQueue<Double>memoryQueue;
        BlockingQueue<Double>cpuTimeQueue;
        BlockingQueue<Double>memoryTimeQueue;
        public InspectNode(String containerName, BlockingQueue<Double>cpuQueue, BlockingQueue<Double>memoryQueue, BlockingQueue<Double>cpuTimeQueue, BlockingQueue<Double>memoryTimeQueue){
            this.containerName = containerName;
            this.cpuQueue = cpuQueue;
            this.memoryQueue = memoryQueue;
            this.cpuTimeQueue = cpuTimeQueue;
            this.memoryTimeQueue = memoryTimeQueue;
        }
    }
    private List<InspectNode>inspectNodes = new ArrayList<>();

    public void stopCollection(){
        shouldStop.set(true);
    }

    public void recoverCollection(){
        shouldStop.set(false);
        beginDockerMetricsCollection(cpuQueueList, memoryQueueList);
        for(InspectNode inspectNode: inspectNodes){
            beginInspectNode(inspectNode.containerName, inspectNode.cpuQueue, inspectNode.memoryQueue, inspectNode.cpuTimeQueue, inspectNode.memoryTimeQueue);
        }
    }

    //cpu and memory usage at this moment, concurrency is supported
    public AtomicDoubleArray cpuUsageArray;
    public AtomicDoubleArray memoryUsageArray;

    //deprecated: have bug
    public void beginDockerMetricsCollection(List<BlockingQueue<Double>>cpuQueueList, List<BlockingQueue<Double>>memoryQueueList){
        cpuUsageArray = new AtomicDoubleArray(DockerRuntimeData.getNodeNameList().size());
        memoryUsageArray = new AtomicDoubleArray(DockerRuntimeData.getNodeNameList().size());
        this.cpuQueueList = cpuQueueList;
        this.memoryQueueList = memoryQueueList;
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
                        if (shouldStop.get()) {
                            System.out.println("Stopping execution...");
                            throw new RuntimeException("Stopping execution...");
                        }

                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
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
                            memoryUsageArray.set(finalI, memoryUsage);
                            cpuUsageArray.set(finalI, cpuUsage * 100);
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
        InspectNode inspectNode = new InspectNode(containerName, cpuQueue, memoryQueue, cpuTimeQueue, memoryTimeQueue);
        inspectNodes.add(inspectNode);
        Thread getterThread =  new Thread(()->{
            AtomicLong startTime = utils.getStartTime();
            StatsCmd statsCmd = dockerClient.statsCmd(containerName);
            statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void close() throws IOException {}
                private Statistics lastStat = null; // 独立的 lastStat
                @Override
                public void onStart(Closeable closeable) {}

                @Override
                public void onNext(Statistics stats) {
                    if (shouldStop.get()) {
                        System.out.println("Stopping execution...");
                        throw new RuntimeException("Stopping execution...");
                    }
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
                        cpuTimeQueue.put( Math.round((currentTime - startTime.get()) / 1000.0 * 10.0) / 10.0);
                        memoryTimeQueue.put( Math.round((currentTime - startTime.get()) / 1000.0 * 10.0) / 10.0);
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
    public void beginDockerMetricsCollectionTry(List<BlockingQueue<Double>> cpuQueueList, List<BlockingQueue<Double>> memoryQueueList) throws Exception {
        Thread getterThread = new Thread(() -> {
            List<String> nodeList = DockerRuntimeData.getNodeNameList();
            ExecutorService executorService = Executors.newFixedThreadPool(nodeList.size()); // 使用线程池并行执行任务
            while (true) {
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < nodeList.size(); i++) {
                    int index = i; // 避免 Lambda 表达式中的变量捕获问题
                    String containerName = nodeList.get(index);
                    futures.add(executorService.submit(() -> {
                        try {
                            ProcessBuilder processBuilder = new ProcessBuilder(
                                    "docker", "stats", containerName, "--no-stream", "--format",
                                    "{{.Container}},{{.CPUPerc}},{{.MemUsage}}"
                            );
                            Process process = processBuilder.start();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith(containerName + ",")) {
                                    String[] stats = line.split(",");
                                    String containerRead = stats[0];

                                    Double cpuUsage = Double.parseDouble(stats[1].split("%")[0]);

                                    Double memUsage;
                                    if (stats[2].contains("GiB / "))
                                        memUsage = Double.parseDouble(stats[2].split("GiB / ")[0]) * 1000.0;
                                    else if (stats[2].contains("MiB / "))
                                        memUsage = Double.parseDouble(stats[2].split("MiB")[0]);
                                    else if (stats[2].contains("KiB / "))
                                        memUsage = Double.parseDouble(stats[2].split("KiB")[0]) / 1000.0;
                                    else
                                        break;

                                    if (containerRead.equals(containerName)) {
                                        BlockingQueue<Double> cpuQueue = cpuQueueList.get(index);
                                        BlockingQueue<Double> memoryQueue = memoryQueueList.get(index);
                                        logger.info("CPU: " + cpuUsage + "%, Memory: " + memUsage + "MB" + " for " + containerName);
                                        cpuQueue.put(cpuUsage);
                                        memoryQueue.put(memUsage / 1024.0);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }));
                }

                // 等待所有任务完成
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
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
    public void changeNetworkOut(String containerName, double outBandwidth, double outLatency) throws IllegalArgumentException {
        if(ExecutorParameters.useFixedLatency){
            return;
        }
        if (outBandwidth <= 0) {
            throw new IllegalArgumentException("Network out bandwidth should be greater than 0");
        }

        try {
            // Construct the command
            String command =
                            "tc class replace dev eth0 parent 1: classid 1:1 htb rate " + outBandwidth + "kbps ceil " + outBandwidth + "kbps && " +
                            "tc qdisc replace dev eth0 parent 1:1 handle 10: netem delay " + outLatency + "ms ";
            // Ensure container exists and is running
            Container container = dockerClient.listContainersCmd()
                    .withNameFilter(Collections.singletonList(containerName))
                    .exec()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Container not found or not running: " + containerName));

            // Create and start the exec command
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(container.getId())
                    .withCmd("bash", "-c", command)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback())
                    .awaitCompletion();

            logger.info("Updated network out bandwidth for container " + containerName + " to " + outBandwidth + " kbps" + " and latency to " + outLatency + " ms");

        } catch (Exception e) {
            logger.error("Failed to update network settings for container " + containerName, e);
            throw new RuntimeException("Failed to update network settings for container " + containerName, e);
        }
    }
    public void changeNetworkIn(String containerName, double inBandwidth, double inLatency) throws IllegalArgumentException {
        if(ExecutorParameters.useFixedLatency){
            return;
        }
        if (inBandwidth <= 0) {
            throw new IllegalArgumentException("Network in bandwidth should be greater than 0");
        }

        try {
            // Construct the command
            String command =
                    "tc class replace dev ifb0 parent 2: classid 2:22 htb rate " + inBandwidth + "kbps ceil " + inBandwidth + "kbps && " +
                    "tc qdisc replace dev ifb0 parent 2:22 handle 20: netem delay " + inLatency + "ms ";
            // Ensure container exists and is running
            Container container = dockerClient.listContainersCmd()
                    .withNameFilter(Collections.singletonList(containerName))
                    .exec()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Container not found or not running: " + containerName));

            // Create and start the exec command
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(container.getId())
                    .withCmd("bash", "-c", command)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback())
                    .awaitCompletion();

            logger.info("Updated network in bandwidth for container " + containerName + " to " + inBandwidth + " kbps" + " and latency to " + inLatency + " ms");

        } catch (Exception e) {
            logger.error("Failed to update network settings for container " + containerName, e);
            throw new RuntimeException("Failed to update network settings for container " + containerName, e);
        }
    }

}
