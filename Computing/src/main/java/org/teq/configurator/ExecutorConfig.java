package org.teq.configurator;

public class ExecutorConfig implements TeqGlobalConfig{
    public static String endDeviceLayerName = "EndDeviceLayer";
    public static String coordinatorLayerName = "CoordinatorLayer";
    public static String workerLayerName = "WorkerLayer";
    public static String dataCenterLayerName = "DataCenterLayer";

    public static int fromNetworkToEndPort = 9999;
    public static int fromEndToCodPort = 10000;
    public static int fromCodToWorkerPort = 10001;
    public static int fromWorkerToCenterPort = 10002;
    public static int fromCenterToWorkerPort = 10003;
    public static int fromWorkerToCodPort = 10004;
    public static int fromCodToEndPort = 10005;

    public static int maxNumRetries = 100;
    public static int retryInterval = 1000;

    public static boolean useFixedLatency = false;
    public static long minBuffer = 200; // unit: ms, 每发出数据条目之间设置的缓冲时间
    public static int waitBeforeStart = 10000; // unit: ms, 在开始发送数据之前等待的时间
}
