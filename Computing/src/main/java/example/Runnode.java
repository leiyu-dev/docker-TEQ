package example;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Runnode {
    public static void main(String[] args) {
        try {
            // 1. 指定 .class 文件所在的目录路径
            String classPath = ".";  // 假设 .class 文件位于 ./out 目录下
            String className = "example.mynode";  // 类名（不带 .class 后缀）

            // 2. 创建 URLClassLoader 加载指定目录中的 .class 文件
            URL[] urls = { new URL("file://" + classPath + "/") };
            URLClassLoader classLoader = new URLClassLoader(urls);

            // 3. 加载指定的类
            Class<?> clazz = classLoader.loadClass(className);

            // 4. 创建类的实例
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 5. 获取类中的方法
            Method method = clazz.getMethod("process");

            // 6. 调用方法
            method.invoke(instance);
            // 关闭类加载器（JVM 会自动回收，但推荐显式关闭）
            classLoader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
