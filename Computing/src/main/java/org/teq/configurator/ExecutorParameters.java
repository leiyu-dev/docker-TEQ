package org.teq.configurator;

public class ExecutorParameters {
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
    public String toString() {
        return "ExecutorParameters{" +
                ", fromEndToCodPort=" + fromEndToCodPort +
                ", fromCodToWorkerPort=" + fromCodToWorkerPort +
                ", fromWorkerToCenterPort=" + fromWorkerToCenterPort +
                ", fromCenterToWorkerPort=" + fromCenterToWorkerPort +
                ", fromWorkerToCodPort=" + fromWorkerToCodPort +
                ", fromCodToEndPort=" + fromCodToEndPort +
                ", maxNumRetries=" + maxNumRetries +
                ", retryInterval=" + retryInterval +
                '}';
    }
}
