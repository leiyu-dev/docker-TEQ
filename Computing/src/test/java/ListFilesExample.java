import com.alibaba.fastjson.JSON;
import org.teq.configurator.SimulatorConfigurator;

import java.io.File;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.teq.utils.StaticSerializer.deserializeFromJson;
import static org.teq.utils.StaticSerializer.serializeToJson;


public class ListFilesExample {


    // 示例类
    public static class MyClass {
        public static int staticInt = 42;
        public static String staticString = "Hello, World!";
        public static boolean staticBoolean = true;
    }
    public static void main(String[] args) throws Exception {

        // 序列化
        String json = serializeToJson(MyClass.class);
        System.out.println("Serialized JSON: " + json);

        // 修改静态变量的值
        MyClass.staticInt = 0;
        MyClass.staticString = "Modified!";
        MyClass.staticBoolean = false;

        // 反序列化
        deserializeFromJson(MyClass.class, json);
        System.out.println("After Deserialization:");
        System.out.println("staticInt: " + MyClass.staticInt);
        System.out.println("staticString: " + MyClass.staticString);
        System.out.println("staticBoolean: " + MyClass.staticBoolean);
    }
}
