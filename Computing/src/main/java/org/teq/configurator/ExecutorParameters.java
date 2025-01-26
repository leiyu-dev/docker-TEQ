package org.teq.configurator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ExecutorParameters implements TeqGlobalConfig{
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

    public static boolean useFixedLatency = true;
    public static long minBuffer = 100; // unit: ms, 每发出数据条目之间设置的缓冲时间

}
