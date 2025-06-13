package org.teq.simulator;

import com.alibaba.fastjson.JSON;
import org.teq.backend.BackendManager;
import org.teq.backend.LogHandler;
import org.teq.configurator.ExecutorConfig;
import org.teq.configurator.SimulatorConfig;
import org.teq.configurator.TeqGlobalConfig;
import org.teq.layer.Layer;
import org.teq.mearsurer.MetricsTransformer;
import org.teq.node.AbstractDockerNode;
import org.teq.node.DefaultDockerNode;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.StaticSerializer;
import org.teq.utils.utils;
import org.teq.visualizer.FileDisplayer;
import org.teq.visualizer.MetricsDisplayer;
import org.teq.visualizer.SocketDisplayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;


/**
 * @author teq
 * @version 0.2
 *
 * This class is the main class of the simulator.It encapsulates all the behavior during the simulation.
 */
public class Simulator {


    //state service
    private AtomicInteger state = new AtomicInteger(0);
    public String getState(){
        if(state.get() == 0){
            return "STOPPED";
        }
        else if(state.get() == 1){
            return "RUNNING";
        }
        else if(state.get() == 2){
            return "RESTARTING";
        }
        else if(state.get() == 3){
            return "STOPPING";
        }
        else {
            return "DISCONNECTED";
        }
    }

    private static final Logger logger = LogManager.getLogger(Simulator.class);
    private int nodeCount = 0;


    //module1: docker runner, used to launch the docker container
    private final DockerRunner dockerRunner;
    public DockerRunner getDockerRunner() {
        return dockerRunner;
    }

    public Simulator(AbstractNetworkHostNode networkHostNode){
        this(networkHostNode,true, false);
    }

    public Simulator(AbstractNetworkHostNode networkHostNode, boolean openWebUI){
        this(networkHostNode,openWebUI,false);
    }

    public Simulator(AbstractNetworkHostNode networkHostNode, boolean openWebUI, boolean useTCPConnection){

        logger.info("Initializing the simulator");
        // this line use TCP connection, if you want to use TCP, uncomment this line
        if(useTCPConnection) {
            dockerRunner = new DockerRunner(SimulatorConfig.imageName, Integer.toString(SimulatorConfig.tcpPort));
        }

        // this line use default connection
        // if you want to use default connection(DOCKER_HOST,Unix Socket(linux),npipe(windows)), uncomment this line
        else {
            dockerRunner = new DockerRunner(SimulatorConfig.imageName);
        }

        //Add the network host node to the node list
        String networkHostName = SimulatorConfig.networkHostName;
        addNode(networkHostNode, networkHostName,NodeType.network);

        addConfig(SimulatorConfig.class);
        addConfig(ExecutorConfig.class);

        if(openWebUI) {
            metricsDisplayer = new SocketDisplayer();
        }
        else{
            metricsDisplayer = new FileDisplayer();
        }

        metricsTransformer = new MetricsTransformer(this, metricsDisplayer);

        logger.info("Initializing success");
    }
    public enum NodeType{
        network,
        normal,
    }
    private class SimulatorNode{
        public DockerNodeParameters parameters;
        public String nodeName;
        public NodeType nodeType;
        public int nodeId;
        SimulatorNode(DockerNodeParameters parameters,String nodeName,NodeType nodeType,int nodeId){
            this.parameters = parameters;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
            this.nodeId = nodeId;
        }
    }
    private class SimulatorLayer{
        public String layerName;
        public int nodeIdBegin;
        public int nodeIdEnd;
        SimulatorLayer(String layerName,int nodeIdBegin,int nodeIdEnd){
            this.layerName = layerName;
            this.nodeIdBegin = nodeIdBegin;
            this.nodeIdEnd = nodeIdEnd;
        }
    }
    private List<Class<? extends TeqGlobalConfig>>configs = new ArrayList<>();
    private List<SimulatorNode> nodes = new ArrayList<>();
    private List<SimulatorLayer>layers = new ArrayList<>();
    private List<DockerNodeParameters> parameters = new ArrayList<>();

    public List<Class<? extends TeqGlobalConfig>> getConfigs() {
        return configs;
    }
    public List<DockerNodeParameters> getParameters() {
        return parameters;
    }
    public int getNodeCount() {
        return nodes.size();
    }

    public int getLayerCount() {
        return layers.size();
    }

    //TODO: implement algorithm level management
    public int getAlgorithmCount() {
        return 1;
    }

    public void addConfig(Class<? extends TeqGlobalConfig> config){
        configs.add(config);
    }

    public void addLayer(Layer layer){
        int nodeCount = layer.getNodeCount();
        StringBuilder nodeNameContent = new StringBuilder();
        String layerNodeNameFile = SimulatorConfig.dataFolderPath + "/" + layer.getLayerName() + "/" +
                SimulatorConfig.nodeNameFileName;
        int nodeIdBegin = this.nodeCount;
        for(int i = 0; i < nodeCount; i++){
            AbstractDockerNode node = layer.getFunctionNode();
            node.parameters = layer.getNodeParameter(i);
            nodeNameContent.append(SimulatorConfig.classNamePrefix).append(layer.getNodeName(i)).append("\n");
            addNode(node,layer.getNodeName(i));
        }
        layers.add(new SimulatorLayer(layer.getLayerName(),nodeIdBegin, this.nodeCount -1));
        utils.writeStringToFile(layerNodeNameFile,nodeNameContent.toString());
    }

    public void addNode(AbstractDockerNode node){
        addNode(node,"");
    }
    public void addNode(AbstractDockerNode node,String nodeName) {
        addNode(node,nodeName,NodeType.normal);
    }
    private void addNode(AbstractDockerNode node,String nodeName,NodeType nodeType){
        Class<AbstractDockerNode>clazz = (Class<AbstractDockerNode>)node.getClass();
        if(nodeName.isEmpty()){
            nodeName = SimulatorConfig.classNamePrefix + nodeCount + "_auto_name";
        }
        else {
            nodeName = SimulatorConfig.classNamePrefix + nodeName;
        }
        logger.info("add node " + nodeName);
        addNodeToStartClass(clazz.getName());
        nodes.add(new SimulatorNode(node.parameters,nodeName,nodeType, nodeCount));
        parameters.add(node.parameters);
        nodeCount++;
    }

    //module2: backend manager, used to launch a server providing backend data to the user interface
    private BackendManager backendManager;
    public BackendManager getBackendManager() {
        return backendManager;
    }

    private MetricsDisplayer metricsDisplayer;
    public MetricsDisplayer getMetricsDisplayer() {
        return metricsDisplayer;
    }
    /**
     * This method is used to set the metrics displayer.
     * It is used when you want to set your own metrics display logic.
     * @param metricsDisplayer
     */
    void setMetricsDisplayer(MetricsDisplayer metricsDisplayer) {
        this.metricsDisplayer = metricsDisplayer;
    }

    private MetricsTransformer metricsTransformer;
    public MetricsTransformer getMetricsTransformer() {
        return metricsTransformer;
    }
    /**
     * This method is used to set the metrics transformer.
     * It is used when you want to set your own metrics transform logic.
     * @param metricsTransformer
     */
    void setMetricsTransformer(MetricsTransformer metricsTransformer) {
        this.metricsTransformer = metricsTransformer;
    }


    //central part: start,stop,restart
    public void start() throws Exception {
        state.set(2);
        utils.startTimer();

        logger.info("Starting the simulation");

        logger.info("launching the backend");
        backendManager = new BackendManager(this);
        backendManager.launch();

        logger.info("generating running script and persistent data");
        runAssembleScript();
        writeStartScriptToFile();
        writeStartClassToFile();
        writeRuntimeData();

        logger.info("Starting the nodes");
        startNodes();

        try {
            metricsTransformer.beginTransform();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        metricsDisplayer.display();


        state.set(1);
        // 等待所有节点运行完
    }

    public void stop(){
        state.set(3);
        dockerRunner.stopCollection();
        dockerRunner.closeAllContainers();
        state.set(0);
    }

    public void restart() throws Exception {
        state.set(2);
        utils.startTimer();
        logger.info("Restarting the simulation");

        dockerRunner.stopCollection();
        dockerRunner.closeAllContainers();

        runAssembleScript();
        writeStartScriptToFile();
        writeStartClassToFile();
        writeRuntimeData();

        logger.info("Starting the nodes");
        startNodes();
        
        Thread thread = new Thread(dockerRunner::recoverCollection);
        thread.start();
        
        // Notify metrics transformer to restart receivers
        if (metricsTransformer != null) {
            logger.info("Notifying metrics transformer to restart");
            metricsTransformer.notifyRestart();
        }
        
        state.set(1);
    }

    private void startNodes() throws Exception {
        // 使用线程池来并行启动容器，线程池大小可根据系统性能调整
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(30, nodes.size()));
        
        try {
            List<Future<?>> futures = new ArrayList<>();
            
            // 为每个节点创建启动任务
            for(SimulatorNode node : nodes){
                futures.add(executorService.submit(() -> {
                    try {
                        DockerNodeParameters parameters = node.parameters;
                        String nodeName = node.nodeName;
                        NodeType nodeType = node.nodeType;
                        int nodeId = node.nodeId;
                        
                        logger.trace("Running container for class " + nodeName);
                        
                        if(nodeType == NodeType.normal) {
                            dockerRunner.createAndStartContainer(nodeName, nodeId, parameters);
                            logger.info("Node " + nodeName + " started");
                        }
                        else if(nodeType == NodeType.network){
                            dockerRunner.runNetworkHostContainer(nodeName, nodeId);
                            logger.info("Network node started");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to start container: " + node.nodeName, e);
                        throw new RuntimeException("Failed to start container: " + node.nodeName, e);
                    }
                }));
            }
            
            // 等待所有容器启动完成
            for (Future<?> future : futures) {
                future.get(); // 这会抛出任何在任务中发生的异常
            }
            
            logger.info("All " + nodes.size() + " containers started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start some containers", e);
            throw new Exception("Failed to start some containers", e);
        } finally {
            executorService.shutdown();
            try {
                // 等待线程池关闭，最多等待30秒
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Thread pool did not terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for thread pool termination");
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    /** module3: Generating the start script and the start class.
     *  Also generating persistent data.
     *  May be moved into a separate class in the future
     */

    private String startClassImportContent = "import org.teq.configurator.*;\n" +
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Path;\n" +
            "import org.teq.utils.*;\n" +
            "import java.util.List;\n" ;
    private String startClassSwitchContent = "";

    private void runAssembleScript() throws IOException {
        //assemble the docker folder
        //TODO:Windows
        logger.info("Assembling docker folder");
        String command = "bash assemble.sh";
        Process process = Runtime.getRuntime().exec(command);

        String line;
        // get the output from the process
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = inputReader.readLine()) != null) {
            logger.debug("Script Output:"+line);
        }

        // 获取命令的错误输出
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        System.out.println("Error Output (if any):");
        while ((line = errorReader.readLine()) != null) {
            logger.error("Script Error:"+line);
        }
        logger.info("Docker folder assembled");

    }




    private void writeStartScriptToFile() throws IOException {
        logger.info("Writing start script to file...");
        String scriptContent = "#!/bin/bash\n" +
                "cd \"$(dirname \"${BASH_SOURCE[0]}\")\"\n" +
                "java -cp ./lib/*:. " + SimulatorConfig.StartPackageName + "." + SimulatorConfig.StartClassName + "\n";

        String fileName = SimulatorConfig.hostPath + "/" + SimulatorConfig.startScriptName;

        utils.writeStringToFile(fileName,scriptContent);
        logger.info("Start script saved as " + fileName);
    }

    private void addNodeToStartClass(String nodeClassName) {
        startClassSwitchContent += "            case " + nodeCount + " :\n" +
                "                node = new " + nodeClassName + "();\n" +
                "                break;\n";
    }
    private boolean addDefault = false;
    private void writeStartClassToFile() throws IOException {
        if(!addDefault) {
            addDefault = true;
            startClassSwitchContent += "            default:\n" +
                    "               node = new " + DefaultDockerNode.class.getName() + "();\n" +
                    "                break;\n";
        }

        logger.info("Writing start class to file...");
        String classContent = "package " + SimulatorConfig.StartPackageName + ";\n" +
                startClassImportContent +
                "public class "+ SimulatorConfig.StartClassName +"{\n" +
                "    public static void main(String[] args) throws Exception{\n" +
                "        Path path = Path.of(SimulatorConfig.dataFolderName + \"/\" + SimulatorConfig.parametersClassFileName);\n" +
                "            List<String> parametersClassNames = Files.readAllLines(path);\n" +
                "            for(int i = 0; i < parametersClassNames.size(); i++){\n" +
                "                if(parametersClassNames.get(i).isEmpty()){\n" +
                "                    continue;\n" +
                "                }\n" +
                "                String parametersClassName = parametersClassNames.get(i);\n" +
                "                String parametersFilePath = SimulatorConfig.dataFolderName + \"/config/\" + parametersClassName + \".json\";\n" +
                "                try {\n" +
                "                    StaticSerializer.deserializeFromJson(Class.forName(parametersClassName),utils.readFirstLineFromFile(parametersFilePath));\n" +
                "                } catch (Exception e) {\n" +
                "                    throw new RuntimeException(e);\n" +
                "                }\n" +
                "            }\n" +
                "        " + AbstractDockerNode.class.getName() + " node;\n" +
                "       switch (Integer.parseInt(System.getenv(\"NODE_ID\"))) {\n" +
                startClassSwitchContent +
                "        }\n" +
                "        node.process();\n" +
                "    }\n" +
                "}\n";

        String fileName = SimulatorConfig.hostPath + "/" +
                SimulatorConfig.StartPackageName.replace(".","/") + "/" +
                SimulatorConfig.StartClassName + ".java";

        utils.writeStringToFile(fileName,classContent);
        logger.info("start class saved as" + fileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            logger.error("Start class: Java compiler not found. Are you running a JRE instead of a JDK?");
            return;
        }

        // 使用编译器编译文件
        int result = compiler.run(null, null, null,"-classpath",
                SimulatorConfig.hostPath + "/", fileName);
        if (result == 0) {
            logger.info(fileName + " compiled successfully");
        } else {
            logger.error(fileName + " compilation failed");
        }

    }

    private void writeRuntimeData(){
        //write the node name file
        String nodeNameFilePath = SimulatorConfig.dataFolderPath + "/" + SimulatorConfig.nodeNameFileName;
        StringBuilder nodeNameContent = new StringBuilder();
        for(SimulatorNode node : nodes){
            nodeNameContent.append(node.nodeName).append("\n");
        }
        utils.writeStringToFile(nodeNameFilePath,nodeNameContent.toString());
        logger.info("Node name file saved as " + nodeNameFilePath);


        //write the layer name file
        String layerNamePath = SimulatorConfig.dataFolderPath + "/" + SimulatorConfig.layerNameFileName;
        StringBuilder layerNameContent = new StringBuilder();
        for(SimulatorLayer layer : layers){
            layerNameContent.append(layer.layerName).append(",").append(layer.nodeIdBegin).append(",").append(layer.nodeIdEnd).append("\n");
        }
        utils.writeStringToFile(layerNamePath,layerNameContent.toString());
        logger.info("Layer name file saved as " + layerNamePath);

        //write the config parameters class name file
        String ParametersPath = SimulatorConfig.dataFolderPath + "/" + SimulatorConfig.parametersClassFileName;
        StringBuilder parametersContent = new StringBuilder();
        for(Class<? extends TeqGlobalConfig> config : configs){
            parametersContent.append(config.getName()).append("\n");
        }
        utils.writeStringToFile(ParametersPath,parametersContent.toString());
        logger.info("Parameters class name file saved as " + ParametersPath);


        //write the config parameters
        utils.writeStringToFile(SimulatorConfig.dataFolderPath + "/config/configs","here are the configs");
        for(Class<? extends TeqGlobalConfig> config : configs){
            String conf = null;
            try {
                conf = StaticSerializer.serializeToJson(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            utils.writeStringToFile(SimulatorConfig.dataFolderPath + "/config/" + config.getName() + ".json",conf);
        }

        //write the node configs
        utils.writeStringToFile(SimulatorConfig.dataFolderPath + "/nodeParams", JSON.toJSONString(parameters));

    }

    private void cleanUp() {
        dockerRunner.cleanUp();
    }
}
