package org.teq.configurator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ExecutorParameters implements InDockerConfig{
    public static String endDeviceLayerName = "EndDeviceLayer";
    public static String coordinatorLayerName = "CoordinatorLayer";
    public static String workerLayerName = "WorkerLayer";
    public static String dataCenterLayerName = "DataCenterLayer";
    public static int fromEndToCodPort = 10000;
    public static int fromCodToWorkerPort = 10001;
    public static int fromWorkerToCenterPort = 10002;
    public static int fromCenterToWorkerPort = 10003;
    public static int fromWorkerToCodPort = 10004;
    public static int fromCodToEndPort = 10005;

    public static int maxNumRetries = 10;
    public static int retryInterval = 100;

    @Override
    public void getFromProperties(String configFile) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configFile));
            endDeviceLayerName = props.getProperty("endDeviceLayerName", endDeviceLayerName);
            coordinatorLayerName = props.getProperty("coordinatorLayerName", coordinatorLayerName);
            workerLayerName = props.getProperty("workerLayerName", workerLayerName);
            dataCenterLayerName = props.getProperty("dataCenterLayerName", dataCenterLayerName);

            fromEndToCodPort = Integer.parseInt(props.getProperty("fromEndToCodPort", String.valueOf(fromEndToCodPort)));
            fromCodToWorkerPort = Integer.parseInt(props.getProperty("fromCodToWorkerPort", String.valueOf(fromCodToWorkerPort)));
            fromWorkerToCenterPort = Integer.parseInt(props.getProperty("fromWorkerToCenterPort", String.valueOf(fromWorkerToCenterPort)));
            fromCenterToWorkerPort = Integer.parseInt(props.getProperty("fromCenterToWorkerPort", String.valueOf(fromCenterToWorkerPort)));
            fromWorkerToCodPort = Integer.parseInt(props.getProperty("fromWorkerToCodPort", String.valueOf(fromWorkerToCodPort)));
            fromCodToEndPort = Integer.parseInt(props.getProperty("fromCodToEndPort", String.valueOf(fromCodToEndPort)));

            maxNumRetries = Integer.parseInt(props.getProperty("maxNumRetries", String.valueOf(maxNumRetries)));
            retryInterval = Integer.parseInt(props.getProperty("retryInterval", String.valueOf(retryInterval)));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveToProperties(String configFile) {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            Properties props = new Properties();

            props.setProperty("endDeviceLayerName", endDeviceLayerName);
            props.setProperty("coordinatorLayerName", coordinatorLayerName);
            props.setProperty("workerLayerName", workerLayerName);
            props.setProperty("dataCenterLayerName", dataCenterLayerName);
            props.setProperty("fromEndToCodPort", String.valueOf(fromEndToCodPort));
            props.setProperty("fromCodToWorkerPort", String.valueOf(fromCodToWorkerPort));
            props.setProperty("fromWorkerToCenterPort", String.valueOf(fromWorkerToCenterPort));
            props.setProperty("fromCenterToWorkerPort", String.valueOf(fromCenterToWorkerPort));
            props.setProperty("fromWorkerToCodPort", String.valueOf(fromWorkerToCodPort));
            props.setProperty("fromCodToEndPort", String.valueOf(fromCodToEndPort));

            props.setProperty("maxNumRetries", String.valueOf(maxNumRetries));
            props.setProperty("retryInterval", String.valueOf(retryInterval));


            props.store(fos, "Executor Parameters");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
