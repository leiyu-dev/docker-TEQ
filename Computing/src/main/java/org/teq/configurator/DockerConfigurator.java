package org.teq.configurator;

public class DockerConfigurator {
    public static String volumePath = "/var/lib/teq";
    public static String volumeFolder = "docker";
    public static String startScriptName = "run.sh";
    public static String StartPackageName = "starter";
    public static String StartClassName = "RunNode";
    public static String imageName = "teq:1.0";
    public static int tcpPort = 2375;
    public static String projectPath = System.getProperty("user.dir");
    public static String hostPath = projectPath + "/" + volumeFolder;
    public static String dataFolder = "data";
    public static String dataPath = hostPath + "/" + dataFolder;
    public static String networkHostName = "-NetworkHost";
    public static String networkName = "teq-network";
    public static String networkSubnet = "10.0.0.0/16";
    public static boolean getStdout = false;

    public static String nodeNameFilePath = "NodeName.txt";
}
