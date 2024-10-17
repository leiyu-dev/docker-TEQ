package org.teq.simulator;

import org.teq.configurator.DockerConfigurator;
import org.teq.node.AbstractFlinkNode;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javassist.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Simulator {
    static String classNamePrefix = "mynode";

    private DockerRunner dockerRunner;

    public Simulator(){
        dockerRunner = new DockerRunner();
        dockerRunner.InitRunnerWithTcpHost(DockerConfigurator.imageName,DockerConfigurator.tcpPort);
    }

    public List<Class<AbstractFlinkNode>> nodes = new ArrayList<>();
    public void addNode(Class<?> clazz){
        if(AbstractFlinkNode.class.isAssignableFrom(clazz)){
            nodes.add((Class<AbstractFlinkNode>) clazz);
        }
       else {
            System.out.println("Class "+ clazz.getName() +" is not a subclass of AbstractFlinkNode");
        }
    }
    public void start() throws Exception {
        System.out.println("Simulator initializing");

        //组件好docker文件夹
        //TODO:LINUX
        String command = "powershell.exe -ExecutionPolicy Bypass -File run.ps1";
        Process process = Runtime.getRuntime().exec(command);



        String line;
        // 获取命令的标准输出
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        System.out.println("Standard Output:");
        while ((line = inputReader.readLine()) != null) {
            System.out.println(line);
        }

        // 获取命令的错误输出
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        System.out.println("Error Output (if any):");
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }


        int numberOfNodes = 0;
        for(Class<?>clazz : nodes){
            numberOfNodes++;
            // 使用Javassist加载这个匿名类
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(clazz.getName());
            String packageName = clazz.getPackageName();
            String className = classNamePrefix + numberOfNodes;
            String dirName = "docker/" + packageName.replace(".","/") + "/";
            String fileName = dirName + className + ".class";

            // 修改类的名字为 'example.mynode'
            ctClass.setName(packageName + "." + className);

            // 将修改后的字节码写入文件
            byte[] byteCode = ctClass.toBytecode();
            saveClassToFile(byteCode, fileName);

            System.out.println("Class saved as " + fileName);

            writeStartClassToFile(DockerConfigurator.hostPath+"/",packageName,className,dirName);

            writeStartScriptToFile(DockerConfigurator.hostPath+"/",packageName,className);

            // 运行该节点的容器
            dockerRunner.RunContainer(className);
        }


    }
    private void writeStartScriptToFile(String dockerFileName,String packageName, String className) throws IOException {
        String scriptContent = "#!/bin/bash\n" +
                "java -cp ./lib/*:. " + packageName + ".Run" + className + "\n";

        String fileName = dockerFileName + "run.sh";

        File sourceFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
            fos.write(scriptContent.getBytes());
        }
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
            System.out.println("无法找到 Java 编译器，请确保您使用的是 JDK 而不是 JRE。");
            return;
        }

        // 使用编译器编译文件
        int result = compiler.run(null, null, null,"-classpath", dockerFileName , sourceFile.getPath());
        if (result == 0) {
            System.out.println("编译成功！");
        } else {
            System.out.println("编译失败！");
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
}
