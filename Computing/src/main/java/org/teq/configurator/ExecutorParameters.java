package org.teq.configurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExecutorParameters {
    private static String defaultConfigFilePath = "./Computing/src/main/resources/";
    private static String fileName;
    public static String file;
    public static int fromEndToCodPort;
    public static int fromCodToWorkerPort;
    public static int fromWorkerToCenterPort;
    public static int fromCenterToWorkerPort;
    public static int fromWorkerToCodPort;
    public static int fromCodToEndPort;

    public static boolean openWebUI;
    public static int endDeviceLayerPort;
    public static int coordinatorLayerPort;
    public static int workerLayerPort;
    public static int dataCenterLayerPort;

    public static int endDevicesNum;
    public static int workersNum;
    public static int dataCenterNum;

    public static int maxNumRetries;
    public static int retryInterval;

    public static int sinkToMeasurerPort;

    @Override
    public String toString() {
        return "ExecutorParameters{" +
                "defaultConfigFilePath='" + defaultConfigFilePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", file='" + file + '\'' +
                ", fromEndToCodPort=" + fromEndToCodPort +
                ", fromCodToWorkerPort=" + fromCodToWorkerPort +
                ", fromWorkerToCenterPort=" + fromWorkerToCenterPort +
                ", fromCenterToWorkerPort=" + fromCenterToWorkerPort +
                ", fromWorkerToCodPort=" + fromWorkerToCodPort +
                ", fromCodToEndPort=" + fromCodToEndPort +
                ", openWebUI=" + openWebUI +
                ", endDeviceLayerPort=" + endDeviceLayerPort +
                ", coordinatorLayerPort=" + coordinatorLayerPort +
                ", workerLayerPort=" + workerLayerPort +
                ", dataCenterLayerPort=" + dataCenterLayerPort +
                ", endDevicesNum=" + endDevicesNum +
                ", workersNum=" + workersNum +
                ", dataCenterNum=" + dataCenterNum +
                ", maxNumRetries=" + maxNumRetries +
                ", retryInterval=" + retryInterval +
                ", sinkToMeasurerPort=" + sinkToMeasurerPort +
                '}';
    }
}
