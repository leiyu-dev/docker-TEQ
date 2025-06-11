package org.teq.configurator;

public class SimulatorConfig implements TeqGlobalConfig{
    public static String volumePath = "/var/lib/teq";
    public static String volumeFolderName = "docker";
    public static String startScriptName = "run.sh";
    public static String StartPackageName = "starter";
    public static String StartClassName = "RunNode";
    public static String imageName = "teq:1.0";
    public static int tcpPort = 2375;

    public static String projectPath = System.getProperty("user.dir");
    public static String hostPath = projectPath + "/" + volumeFolderName;
    public static String dataFolderName = "data";
    public static String dataFolderPath = hostPath + "/" + dataFolderName;
    public static boolean getStdout = false;

    public static String nodeNameFileName = "NodeName.txt";
    public static String layerNameFileName = "LayerName.txt";
    public static String parametersClassFileName = "ParameterClasses.txt";
    public static String hostIpFileName = "HostIp.txt";
    public static String defaultLayerNamePrefix = "layer";

    /**
     *   WARNING: the networkHostName is only used in Simulator to generate the true networkHostNodeName,
     *   do not use it as host name in DataSender
     */
    public static String networkHostName = "NetworkHost";
    public static String networkName = "teq-network";
    public static String networkSubnet = "10.0.0.0/16";
    public static String networkGateway = "10.0.0.2";
    public static int metricsPort = 50000;
    public static int MetricsReceiverPort = 8888;
    public static int restfulPort = 8889;
    public static String classNamePrefix = "teq_node_";
    public static int displayInterval = 3000;
}
