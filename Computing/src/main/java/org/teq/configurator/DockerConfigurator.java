package org.teq.configurator;

public class DockerConfigurator {
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
    public static String hostIpFileName = "HostIp.txt";
}
