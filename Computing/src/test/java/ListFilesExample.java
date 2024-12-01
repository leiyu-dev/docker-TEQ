import com.alibaba.fastjson.JSON;
import org.teq.configurator.SimulatorConfigurator;

import java.io.File;

public class ListFilesExample {
    public static void main(String[] args) {
        var conf = new SimulatorConfigurator();
        conf.saveToProperties("Computing/src/main/resources/config.properties");
    }
}
