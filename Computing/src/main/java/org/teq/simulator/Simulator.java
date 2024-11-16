package org.teq.simulator;

import org.teq.configurator.DockerConfigurator;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.node.AbstractDockerNode;
import org.teq.node.AbstractFlinkNode;
import org.teq.simulator.docker.DockerRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javassist.*;
import org.teq.simulator.network.NetworkHostNode;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {

    private static final Logger logger = LogManager.getLogger(Simulator.class);
    private int numberOfNodes = 0;
    private final DockerRunner dockerRunner;

    public Simulator() throws Exception {
        logger.info("Initializing the simulator");
        // this line use TCP connection, if you want to use TCP, uncomment this line
        // dockerRunner = new DockerRunner(DockerConfigurator.imageName,DockerConfigurator.tcpPort);

        // this line use default connection
        // if you want to use default connection(DOCKER_HOST,Unix Socket(linux),npipe(windows)), uncomment this line
        dockerRunner = new DockerRunner(DockerConfigurator.imageName);
        startNetworkHostNode(NetworkHostNode.class);
    }

    public List<Class<AbstractDockerNode>> nodes = new ArrayList<>();
    public void addNode(AbstractDockerNode node){
        Class<?>clazz = node.getClass();
        if(AbstractDockerNode.class.isAssignableFrom(clazz)){
            nodes.add((Class<AbstractDockerNode>) clazz);
        }
       else {
            logger.error("Class "+ clazz.getName() +" is not a subclass of AbstractFlinkNode");
        }
    }

    public void start() throws Exception {
        logger.info("Starting the simulation");

        runAssembleScript();

        logger.info("Starting the nodes");

        for(Class<AbstractDockerNode>clazz : nodes){
            startNode(clazz);
        }

        // 等待所有节点运行完
        dockerRunner.waitUntilContainerStopped();
        logger.info("Simulation finished");
        if(SimulatorConfigurator.cleanUpAfterSimulation == true){
            cleanUp();
        }
    }

    private void runAssembleScript() throws IOException {
        //assemble the docker folder
        //TODO:Windows
        logger.info("Assembling docker folder");
//        String command = "powershell.exe -ExecutionPolicy Bypass -File run.ps1";
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

    private void startNode(Class<AbstractDockerNode>clazz) throws Exception {
        startNode(clazz,"");
    }

    private void assembleFile(Class<AbstractDockerNode>clazz,String className)throws Exception{
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(clazz.getName());
        String packageName = clazz.getPackageName();
        if(className == ""){
            className = SimulatorConfigurator.classNamePrefix + numberOfNodes;    
        }
        logger.info("Assembling class " + className);
        String dirName = "docker/" + packageName.replace(".","/") + "/";
        String fileName = dirName + className + ".class";
        ctClass.setName(packageName + "." + className);

        // 修改节点的 getNodeID 方法
        CtClass abstractDockerNode = pool.get(AbstractDockerNode.class.getName());
        CtMethod getNodeIDMethod = abstractDockerNode.getDeclaredMethod("getNodeID");
        getNodeIDMethod.setBody("{ return " + numberOfNodes + "; }");
        byte[] byteCodeGetNodeID = abstractDockerNode.toBytecode();
        String getNodeIDFileName = "docker/" + abstractDockerNode.getPackageName().replace(".","/") + "/" + abstractDockerNode.getSimpleName() + ".class";
        saveClassToFile(byteCodeGetNodeID, getNodeIDFileName);
        // print sd
        // 将修改后的字节码写入文件
        byte[] byteCode = ctClass.toBytecode();
        saveClassToFile(byteCode, fileName);

        logger.info("Class saved as " + fileName);

        logger.debug("Writing start class to file");
        writeStartClassToFile(DockerConfigurator.hostPath+"/",packageName,className,dirName);

        logger.debug("Writing start script to file");
        writeStartScriptToFile(DockerConfigurator.hostPath+"/",packageName,className);
        numberOfNodes++;
    }

    private void startNode(Class<AbstractDockerNode>clazz, String className) throws Exception {
        assembleFile(clazz,className);

        // 运行该节点的容器
        logger.debug("Running container for class " + className);
        dockerRunner.runContainer(className);

        logger.info("Node " + className + " started");
    }

    private void startNetworkHostNode(Class<?>clazz) throws Exception {
        if(!AbstractDockerNode.class.isAssignableFrom(clazz)){
            throw new Exception("Class "+ clazz.getName() +" is not a subclass of AbstractDockerNode");
        }
        String networkHostName = SimulatorConfigurator.classNamePrefix + DockerConfigurator.networkHostName;
        assembleFile((Class<AbstractDockerNode>) clazz, networkHostName);
        logger.info("Running network host container: " + networkHostName);
        dockerRunner.runNetworkHostContainer(networkHostName);
    }

    private void writeStartScriptToFile(String dockerFileName,String packageName, String className) throws IOException {
        String scriptContent = "#!/bin/bash\n" +
                "cd \"$(dirname \"${BASH_SOURCE[0]}\")\"\n" +
                "java -cp ./lib/*:. " + packageName + ".Run" + className + "\n";

        String fileName = dockerFileName + DockerConfigurator.startScriptName;

        File sourceFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(scriptContent.getBytes());
        }
        logger.info("Start script saved as " + fileName);
    }

    private void writeStartClassToFile(String dockerFileName, String packageName,String nodeClassName, String dirName) throws IOException {
        String classContent = "package " + packageName + ";\n" +
                "import " + packageName + "." + nodeClassName + ";\n" +
                "public class Run" + nodeClassName + " {\n" +
                "    public static void main(String[] args) {\n" +
                "        " + nodeClassName + " node = new " + nodeClassName + "();\n" +
                "        node.process();\n" +
                "    }\n" +
                "}\n";

        String fileName = dirName + "Run" + nodeClassName + ".java";

        File sourceFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(classContent.getBytes());
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            logger.error("Start class: Java compiler not found. Are you running a JRE instead of a JDK?");
            return;
        }

        // 使用编译器编译文件
        int result = compiler.run(null, null, null,"-classpath", dockerFileName , sourceFile.getPath());
        if (result == 0) {
            logger.info(fileName + " compiled successfully");
        } else {
            logger.error(fileName + " compilation failed");
        }

        // 删除临时的 .java 源文件
        sourceFile.delete();

    }

    private static void saveClassToFile(byte[] classData, String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // 创建父目录
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(classData);
        }
    }

    private void cleanUp() {
        dockerRunner.cleanUp();
    }
}
