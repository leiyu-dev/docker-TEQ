package org.teq.configurator;

public class DockerConfigurator {
    public static String volumePath = "/var/lib/teq";
    public static String volumeFolder = "docker";
    public static String startScript = "run.sh";
    public static String imageName = "maven:3.9.9-amazoncorretto-11-debian";
    public static int tcpPort = 2375;
    public static String projectPath = System.getProperty("user.dir");
    public static String hostPath = projectPath + "/" + volumeFolder;

    public static boolean getStdout = false;
}
