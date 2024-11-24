import com.alibaba.fastjson.JSON;

import java.io.File;

public class ListFilesExample {
    public static void main(String[] args) {
        String a = "1111222";
        System.out.println(JSON.parseObject(a, String.class));
    }
}
