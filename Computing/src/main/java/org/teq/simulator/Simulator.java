package org.teq.simulator;

import org.teq.node.AbstractFlinkNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    public void start(){
        System.out.println("Simulator started");
        for(Class<?>clazz : nodes){
            System.out.println("Methods of "+ clazz.getName() +" class:");
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                System.out.println(method.getName());
            }
            saveClassToFile(clazz);
            System.out.println("==================\n");
        }
    }
    static int tot_node=0;
    public void saveClassToFile(Class<?> anonymousClass){
        // 获取类的类加载器
        ClassLoader classLoader = anonymousClass.getClassLoader();

        // 生成类文件的路径
        String classFilePath = anonymousClass.getName().replace('.', '/') + ".class";

        // 从类加载器中获取.class文件的字节码
        InputStream classStream = classLoader.getResourceAsStream(classFilePath);

        if (classStream == null) {
            System.out.println("Class file not found for: " + classFilePath);
            return;
        }

        // 将字节码保存到一个独立的.class文件
        try {
            File outputFile = new File("node"+(tot_node++) + ".class");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = ((InputStream) classStream).read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Saved class file to: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                classStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
