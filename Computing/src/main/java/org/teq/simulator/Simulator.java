package org.teq.simulator;

import org.apache.commons.lang3.tuple.Pair;
import org.teq.configurator.DockerConfigurator;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.node.AbstractDockerNode;
import org.teq.node.AbstractFlinkNode;
import org.teq.node.DefaultDockerNode;
import org.teq.simulator.docker.DockerRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javassist.*;
import org.teq.simulator.network.NetworkHostNode;

import javax.print.Doc;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {

    private static final Logger logger = LogManager.getLogger(Simulator.class);
    private int numberOfNodes = 0;
    private final DockerRunner dockerRunner;

    private String startClassImportContent = "";
    private String startClassSwitchContent = "";

    public Simulator() throws Exception {
        logger.info("Initializing the simulator");
        // this line use TCP connection, if you want to use TCP, uncomment this line
        // dockerRunner = new DockerRunner(DockerConfigurator.imageName,DockerConfigurator.tcpPort);

        // this line use default connection
        // if you want to use default connection(DOCKER_HOST,Unix Socket(linux),npipe(windows)), uncomment this line
        dockerRunner = new DockerRunner(DockerConfigurator.imageName);

        //Add the network host node to the node list
        String networkHostName = SimulatorConfigurator.classNamePrefix + DockerConfigurator.networkHostName;
        addNode(NetworkHostNode.class,networkHostName,NodeType.network);

        logger.info("Initializing success");
    }
    public enum NodeType{
        network,
        normal,
    }
    private class Node{
        public Class<? extends AbstractDockerNode>nodeClass;
        public String nodeName;
        public NodeType nodeType;
        public int nodeId;
        Node(Class<? extends AbstractDockerNode>nodeClass,String nodeName,NodeType nodeType,int nodeId){
            this.nodeClass = nodeClass;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
            this.nodeId = nodeId;
        }
    }
    private List<Node> nodes = new ArrayList<>();

    public void addNode(AbstractDockerNode node){
        addNode(node,"");
    }
    public void addNode(AbstractDockerNode node,String nodeName) {
        addNode(node.getClass(),nodeName,NodeType.normal);
    }
    private void addNode(Class<? extends AbstractDockerNode>clazz,String nodeName,NodeType nodeType){
        if(nodeName.isEmpty()){
            nodeName = SimulatorConfigurator.classNamePrefix + numberOfNodes;
        }
        logger.debug("add node" + nodeName);
        addNodeToStartClass(clazz.getName());
        nodes.add(new Node(clazz,nodeName,nodeType,numberOfNodes));
        numberOfNodes++;
    }

    public void start() throws Exception {
        logger.info("Starting the simulation");

        runAssembleScript();
        writeStartScriptToFile();
        writeStartClassToFile();


        logger.info("Starting the nodes");
        for(Node node : nodes){
            String nodeName = node.nodeName;
            NodeType nodeType = node.nodeType;
            int nodeId = node.nodeId;
            logger.debug("Running container for class " + nodeName);
            if(nodeType == NodeType.normal) {
                dockerRunner.runContainer(nodeName, nodeId);
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
                "java -cp ./lib/*:. " + DockerConfigurator.StartPackageName + "." + DockerConfigurator.StartClassName + "\n";

        String fileName = DockerConfigurator.hostPath + "/" + DockerConfigurator.startScriptName;

        File sourceFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(scriptContent.getBytes());
        }
        logger.info("Start script saved as " + fileName);
    }

    private void addNodeToStartClass(String nodeClassName) {
//        startClassImportContent += "import " + packageName + "." + nodeClassName + ";\n";
        startClassSwitchContent += "            case " + numberOfNodes + " :\n" +
                "                node = new " + nodeClassName + "();\n" +
                "                break;\n";
    }

    private void writeStartClassToFile() throws IOException {
        startClassSwitchContent += "            default:\n" +
                "               node = new " + DefaultDockerNode.class.getName() + "();\n" +
                "                break;\n";

        logger.info("Writing start class to file...");
        String classContent = "package " + DockerConfigurator.StartPackageName + ";\n" +
                startClassImportContent +
                "public class "+ DockerConfigurator.StartClassName +"{\n" +
                "    public static void main(String[] args) {\n" +
                "        " + AbstractDockerNode.class.getName() + " node;\n" +
                "       switch (Integer.parseInt(System.getenv(\"NODE_ID\"))) {\n" +
                startClassSwitchContent +
                "        }\n" +
                "        node.process();\n" +
                "    }\n" +
                "}\n";

        String fileName = DockerConfigurator.hostPath + "/" +
                DockerConfigurator.StartPackageName.replace(".","/") + "/" +
                DockerConfigurator.StartClassName + ".java";

        File sourceFile = new File(fileName);
        File parentDir = sourceFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("Failed to create directories: " + parentDir);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(classContent.getBytes());
        }
        logger.info("start class saved as" + fileName);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            logger.error("Start class: Java compiler not found. Are you running a JRE instead of a JDK?");
            return;
        }

        // 使用编译器编译文件
        int result = compiler.run(null, null, null,"-classpath",
                DockerConfigurator.hostPath + "/", sourceFile.getPath());
        if (result == 0) {
            logger.info(fileName + " compiled successfully");
        } else {
            logger.error(fileName + " compilation failed");
        }

        // 删除临时的 .java 源文件
        sourceFile.delete();

    }


    private void cleanUp() {
        dockerRunner.cleanUp();
    }
}
