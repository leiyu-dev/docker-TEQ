package org.teq.simulator;

import org.teq.backend.BackendManager;
import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.SimulatorConfigurator;
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
import org.teq.visualizer.SocketDisplayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {

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
    private final DockerRunner dockerRunner;
    public DockerRunner getDockerRunner() {
        return dockerRunner;
    }
    private String startClassImportContent = "import org.teq.configurator.*;\n" +
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Path;\n" +
            "import org.teq.utils.*;\n" +
            "import java.util.List;\n" ;
    private String startClassSwitchContent = "";
    private BackendManager backendManager = new BackendManager(this);

    public Simulator(AbstractNetworkHostNode networkHostNode){

        logger.info("Initializing the simulator");
        // this line use TCP connection, if you want to use TCP, uncomment this line
        // dockerRunner = new DockerRunner(DockerConfigurator.imageName,DockerConfigurator.tcpPort);

        // this line use default connection
        // if you want to use default connection(DOCKER_HOST,Unix Socket(linux),npipe(windows)), uncomment this line
        dockerRunner = new DockerRunner(SimulatorConfigurator.imageName);

        //Add the network host node to the node list
        String networkHostName = SimulatorConfigurator.networkHostName;
        addNode(networkHostNode, networkHostName,NodeType.network);

        addConfig(SimulatorConfigurator.class);
        addConfig(ExecutorParameters.class);

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

    public int getAlgorithmCount() {
        return 1;
    }

    public void addConfig(Class<? extends TeqGlobalConfig> config){
        configs.add(config);
    }

    public void addLayer(Layer layer){
        int nodeCount = layer.getNodeCount();
        StringBuilder nodeNameContent = new StringBuilder();
        String layerNodeNameFile = SimulatorConfigurator.dataFolderPath + "/" + layer.getLayerName() + "/" +
                SimulatorConfigurator.nodeNameFileName;
        int nodeIdBegin = this.nodeCount;
        for(int i = 0; i < nodeCount; i++){
            AbstractDockerNode node = layer.getFunctionNode();
            node.parameters = layer.getNodeParameter(i);
            nodeNameContent.append(SimulatorConfigurator.classNamePrefix).append(layer.getNodeName(i)).append("\n");
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
            nodeName = SimulatorConfigurator.classNamePrefix + nodeCount + "_auto_name";
        }
        else {
            nodeName = SimulatorConfigurator.classNamePrefix + nodeName;
        }
        logger.debug("add node " + nodeName);
        addNodeToStartClass(clazz.getName());
        nodes.add(new SimulatorNode(node.parameters,nodeName,nodeType, nodeCount));
        parameters.add(node.parameters);
        nodeCount++;
    }

    public void start() throws Exception {
        state.set(2);
        utils.startTimer();
        logger.info("Starting the simulation");

        logger.info("launching the backend");
        backendManager.launch();

        runAssembleScript();
        writeStartScriptToFile();
        writeStartClassToFile();
        writeRuntimeData();

        logger.info("Starting the nodes");
        for(SimulatorNode node : nodes){
            DockerNodeParameters parameters = node.parameters;
            String nodeName = node.nodeName;
            NodeType nodeType = node.nodeType;
            int nodeId = node.nodeId;
            logger.debug("Running container for class " + nodeName);
            if(nodeType == NodeType.normal) {
                dockerRunner.createAndStartContainer(nodeName, nodeId, parameters);
                logger.info("Node " + nodeName + " started");
            }
            else if(nodeType == NodeType.network){
                dockerRunner.runNetworkHostContainer(nodeName, nodeId);
                logger.info("Network node started");
            }
        }

        SocketDisplayer fileDisplayer = new SocketDisplayer();
        MetricsTransformer transformer = new MetricsTransformer(this, fileDisplayer);
        try {
            transformer.beginTransform();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fileDisplayer.display();

        state.set(1);
        // 等待所有节点运行完
        if(SimulatorConfigurator.cleanUpAfterSimulation){
            cleanUp();
        }
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
        for(SimulatorNode node : nodes){
            DockerNodeParameters parameters = node.parameters;
            String nodeName = node.nodeName;
            NodeType nodeType = node.nodeType;
            int nodeId = node.nodeId;
            logger.debug("Running container for class " + nodeName);
            if(nodeType == NodeType.normal) {
                dockerRunner.startContainer(nodeName);
                logger.info("Node " + nodeName + " started");
            }
            else if(nodeType == NodeType.network){
                dockerRunner.startContainer(nodeName);
                logger.info("Network node started");
            }
        }
        Thread thread = new Thread(dockerRunner::recoverCollection);
        thread.start();
        state.set(1);
    }

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
                "java -cp ./lib/*:. " + SimulatorConfigurator.StartPackageName + "." + SimulatorConfigurator.StartClassName + "\n";

        String fileName = SimulatorConfigurator.hostPath + "/" + SimulatorConfigurator.startScriptName;

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
        String classContent = "package " + SimulatorConfigurator.StartPackageName + ";\n" +
                startClassImportContent +
                "public class "+ SimulatorConfigurator.StartClassName +"{\n" +
                "    public static void main(String[] args) throws Exception{\n" +
                "        Path path = Path.of(SimulatorConfigurator.dataFolderName + \"/\" + SimulatorConfigurator.parametersClassFileName);\n" +
                "            List<String> parametersClassNames = Files.readAllLines(path);\n" +
                "            for(int i = 0; i < parametersClassNames.size(); i++){\n" +
                "                if(parametersClassNames.get(i).isEmpty()){\n" +
                "                    continue;\n" +
                "                }\n" +
                "                String parametersClassName = parametersClassNames.get(i);\n" +
                "                String parametersFilePath = SimulatorConfigurator.dataFolderName + \"/config/\" + parametersClassName + \".json\";\n" +
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

        String fileName = SimulatorConfigurator.hostPath + "/" +
                SimulatorConfigurator.StartPackageName.replace(".","/") + "/" +
                SimulatorConfigurator.StartClassName + ".java";

        utils.writeStringToFile(fileName,classContent);
        logger.info("start class saved as" + fileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            logger.error("Start class: Java compiler not found. Are you running a JRE instead of a JDK?");
            return;
        }

        // 使用编译器编译文件
        int result = compiler.run(null, null, null,"-classpath",
                SimulatorConfigurator.hostPath + "/", fileName);
        if (result == 0) {
            logger.info(fileName + " compiled successfully");
        } else {
            logger.error(fileName + " compilation failed");
        }

    }

    private void writeRuntimeData(){
        //write the node name file
        String nodeNameFilePath = SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.nodeNameFileName;
        StringBuilder nodeNameContent = new StringBuilder();
        for(SimulatorNode node : nodes){
            nodeNameContent.append(node.nodeName).append("\n");
        }
        utils.writeStringToFile(nodeNameFilePath,nodeNameContent.toString());
        logger.info("Node name file saved as " + nodeNameFilePath);


        //write the layer name file
        String layerNamePath = SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.layerNameFileName;
        StringBuilder layerNameContent = new StringBuilder();
        for(SimulatorLayer layer : layers){
            layerNameContent.append(layer.layerName).append(",").append(layer.nodeIdBegin).append(",").append(layer.nodeIdEnd).append("\n");
        }
        utils.writeStringToFile(layerNamePath,layerNameContent.toString());
        logger.info("Layer name file saved as " + layerNamePath);

        //write the parameters class name file
        String ParametersPath = SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.parametersClassFileName;
        StringBuilder parametersContent = new StringBuilder();
        for(Class<? extends TeqGlobalConfig> config : configs){
            parametersContent.append(config.getName()).append("\n");
        }
        utils.writeStringToFile(ParametersPath,parametersContent.toString());
        logger.info("Parameters class name file saved as " + ParametersPath);


        //write the parameters
        utils.writeStringToFile(SimulatorConfigurator.dataFolderPath + "/config/configs","here are the configs");
        for(Class<? extends TeqGlobalConfig> config : configs){
            String conf = null;
            try {
                conf = StaticSerializer.serializeToJson(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            utils.writeStringToFile(SimulatorConfigurator.dataFolderPath + "/config/" + config.getName() + ".json",conf);
        }

    }

    private void cleanUp() {
        dockerRunner.cleanUp();
    }
}
