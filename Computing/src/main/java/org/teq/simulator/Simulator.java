package org.teq.simulator;

import org.teq.configurator.ExecutorParameters;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.layer.Layer;
import org.teq.node.AbstractDockerNode;
import org.teq.node.DefaultDockerNode;
import org.teq.node.DockerNodeParameters;
import org.teq.simulator.docker.DockerRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.network.AbstractNetworkHostNode;
import org.teq.utils.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {

    private static final Logger logger = LogManager.getLogger(Simulator.class);
    private int numberOfNodes = 0;
    private final DockerRunner dockerRunner;

    private String startClassImportContent = "import org.teq.configurator.*;";
    private String startClassSwitchContent = "";

    public Simulator(AbstractNetworkHostNode networkHostNode) throws Exception {
        logger.info("Initializing the simulator");
        // this line use TCP connection, if you want to use TCP, uncomment this line
        // dockerRunner = new DockerRunner(DockerConfigurator.imageName,DockerConfigurator.tcpPort);

        // this line use default connection
        // if you want to use default connection(DOCKER_HOST,Unix Socket(linux),npipe(windows)), uncomment this line
        dockerRunner = new DockerRunner(SimulatorConfigurator.imageName);

        //Add the network host node to the node list
        String networkHostName = SimulatorConfigurator.networkHostName;
        addNode(networkHostNode, networkHostName,NodeType.network);

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
    private List<SimulatorNode> nodes = new ArrayList<>();
    private List<SimulatorLayer>layers = new ArrayList<>();
    public void addLayer(Layer layer){
        int nodeCount = layer.getNodeCount();
        StringBuilder nodeNameContent = new StringBuilder();
        String layerNodeNameFile = SimulatorConfigurator.dataFolderPath + "/" + layer.getLayerName() + "/" +
                SimulatorConfigurator.nodeNameFileName;
        int nodeIdBegin = numberOfNodes;
        for(int i = 0; i < nodeCount; i++){
            AbstractDockerNode node = layer.getFunctionNode();
            node.parameters = layer.getNodeParameter(i);
            nodeNameContent.append(SimulatorConfigurator.classNamePrefix).append(layer.getNodeName(i)).append("\n");
            addNode(node,layer.getNodeName(i));
        }
        layers.add(new SimulatorLayer(layer.getLayerName(),nodeIdBegin,numberOfNodes-1));
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
            nodeName = SimulatorConfigurator.classNamePrefix + numberOfNodes + "_auto_name";
        }
        else {
            nodeName = SimulatorConfigurator.classNamePrefix + nodeName;
        }
        logger.debug("add node " + nodeName);
        addNodeToStartClass(clazz.getName());
        nodes.add(new SimulatorNode(node.parameters,nodeName,nodeType,numberOfNodes));
        numberOfNodes++;
    }

    public void start() throws Exception {
        logger.info("Starting the simulation");
        utils.writeStringToFile(SimulatorConfigurator.dataFolderPath + "/config/configs","here are the configs");
        var conf1 = new ExecutorParameters();
        conf1.saveToProperties(SimulatorConfigurator.dataFolderPath + "/config/ExecutorParameters.properties");
        var conf2 = new SimulatorConfigurator();
        conf2.saveToProperties(SimulatorConfigurator.dataFolderPath + "/config/SimulatorConfigurator.properties");
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
                dockerRunner.runContainer(nodeName, nodeId, parameters);
                logger.info("Node " + nodeName + " started");
            }
            else if(nodeType == NodeType.network){
                dockerRunner.runNetworkHostContainer(nodeName, nodeId);
                logger.info("Network node started");
            }
        }


        // 等待所有节点运行完
        dockerRunner.waitUntilContainerStopped();
        logger.info("Simulation finished");
        if(SimulatorConfigurator.cleanUpAfterSimulation){
            cleanUp();
        }
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
        startClassSwitchContent += "            case " + numberOfNodes + " :\n" +
                "                node = new " + nodeClassName + "();\n" +
                "                break;\n";
    }

    private void writeStartClassToFile() throws IOException {
        startClassSwitchContent += "            default:\n" +
                "               node = new " + DefaultDockerNode.class.getName() + "();\n" +
                "                break;\n";

        logger.info("Writing start class to file...");
        String classContent = "package " + SimulatorConfigurator.StartPackageName + ";\n" +
                startClassImportContent +
                "public class "+ SimulatorConfigurator.StartClassName +"{\n" +
                "    public static void main(String[] args) {\n" +
                "        var conf1 = new ExecutorParameters();\n" +
                "        conf1.getFromProperties(SimulatorConfigurator.dataFolderName + \"/config/ExecutorParameters.properties\");\n" +
                "        var conf2 = new SimulatorConfigurator();\n" +
                "        conf2.getFromProperties(SimulatorConfigurator.dataFolderName + \"/config/SimulatorConfigurator.properties\");\n" +
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
        String nodeNameFilePath = SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.nodeNameFileName;
        StringBuilder nodeNameContent = new StringBuilder();
        for(SimulatorNode node : nodes){
            nodeNameContent.append(node.nodeName).append("\n");
        }
        utils.writeStringToFile(nodeNameFilePath,nodeNameContent.toString());
        logger.info("Node name file saved as " + nodeNameFilePath);

        String layerNamePath = SimulatorConfigurator.dataFolderPath + "/" + SimulatorConfigurator.layerNameFileName;
        StringBuilder layerNameContent = new StringBuilder();
        for(SimulatorLayer layer : layers){
            layerNameContent.append(layer.layerName).append(",").append(layer.nodeIdBegin).append(",").append(layer.nodeIdEnd).append("\n");
        }
        utils.writeStringToFile(layerNamePath,layerNameContent.toString());
        logger.info("Layer name file saved as " + layerNamePath);
    }

    private void cleanUp() {
        dockerRunner.cleanUp();
    }
}
