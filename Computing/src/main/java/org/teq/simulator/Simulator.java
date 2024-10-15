package org.teq.simulator;

import org.teq.node.AbstractFlinkNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javassist.*;

public class Simulator {
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
        System.out.println("Simulator started");
        for(Class<?>clazz : nodes){
            // 使用Javassist加载这个匿名类
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get(clazz.getName());

            // 修改类的名字为 'example.mynode'
            ctClass.setName("example.mynode");

            // 将修改后的字节码写入文件
            byte[] byteCode = ctClass.toBytecode();
            saveClassToFile(byteCode, "docker/example/mynode.class");

            System.out.println("Class saved as 'docker/example/mynode.class'");
        }
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
