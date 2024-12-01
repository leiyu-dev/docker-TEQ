package org.teq.configurator;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SimulatorConfigurator implements InDockerConfig {
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
    public static String defaultLayerNamePrefix = "layer";

    /**
     *   WARNING: the networkHostName is only used in Simulator to generate the true networkHostNodeName,
     *   do not use it as host name in DataSender
     */
    public static String networkHostName = "NetworkHost";
    public static String networkName = "teq-network";
    public static String networkSubnet = "10.0.0.0/16";
    public static String networkGateway = "10.0.0.2";
    public static int metricsPortBegin = 50000;
    public static int HostReceiverPort = 8888;
    public static int NetworkHostNodeSenderPort = 8888;
    public static String classNamePrefix = "teq_node_";
    public static boolean cleanUpAfterSimulation = false;


    @Override
    public void getFromProperties(String configFile) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(configFile));
            volumePath = props.getProperty("volumePath", volumePath);
            volumeFolderName = props.getProperty("volumeFolderName", volumeFolderName);
            startScriptName = props.getProperty("startScriptName", startScriptName);
            StartPackageName = props.getProperty("StartPackageName", StartPackageName);
            StartClassName = props.getProperty("StartClassName", StartClassName);
            imageName = props.getProperty("imageName", imageName);
            tcpPort = Integer.parseInt(props.getProperty("tcpPort", String.valueOf(tcpPort)));

            projectPath = props.getProperty("projectPath", projectPath);
            hostPath = props.getProperty("hostPath", hostPath);
            dataFolderName = props.getProperty("dataFolderName", dataFolderName);
            dataFolderPath = props.getProperty("dataFolderPath", dataFolderPath);
            getStdout = Boolean.parseBoolean(props.getProperty("getStdout", String.valueOf(getStdout)));

            nodeNameFileName = props.getProperty("nodeNameFileName", nodeNameFileName);
            layerNameFileName = props.getProperty("layerNameFileName", layerNameFileName);
            hostIpFileName = props.getProperty("hostIpFileName", hostIpFileName);
            defaultLayerNamePrefix = props.getProperty("defaultLayerNamePrefix", defaultLayerNamePrefix);

            networkHostName = props.getProperty("networkHostName", networkHostName);
            networkName = props.getProperty("networkName", networkName);
            networkSubnet = props.getProperty("networkSubnet", networkSubnet);
            networkGateway = props.getProperty("networkGateway", networkGateway);
            metricsPortBegin = Integer.parseInt(props.getProperty("metricsPortBegin", String.valueOf(metricsPortBegin)));
            HostReceiverPort = Integer.parseInt(props.getProperty("HostReceiverPort", String.valueOf(HostReceiverPort)));
            NetworkHostNodeSenderPort = Integer.parseInt(props.getProperty("NetworkHostNodeSenderPort", String.valueOf(NetworkHostNodeSenderPort)));
            classNamePrefix = props.getProperty("classNamePrefix", classNamePrefix);
            cleanUpAfterSimulation = Boolean.parseBoolean(props.getProperty("cleanUpAfterSimulation", String.valueOf(cleanUpAfterSimulation)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveToProperties(String configFile) {
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            Properties props = new Properties();

            props.setProperty("volumePath", volumePath);
            props.setProperty("volumeFolderName", volumeFolderName);
            props.setProperty("startScriptName", startScriptName);
            props.setProperty("StartPackageName", StartPackageName);
            props.setProperty("StartClassName", StartClassName);
            props.setProperty("imageName", imageName);
            props.setProperty("tcpPort", String.valueOf(tcpPort));

            props.setProperty("projectPath", projectPath);
            props.setProperty("hostPath", hostPath);
            props.setProperty("dataFolderName", dataFolderName);
            props.setProperty("dataFolderPath", dataFolderPath);
            props.setProperty("getStdout", String.valueOf(getStdout));

            props.setProperty("nodeNameFileName", nodeNameFileName);
            props.setProperty("layerNameFileName", layerNameFileName);
            props.setProperty("hostIpFileName", hostIpFileName);
            props.setProperty("defaultLayerNamePrefix", defaultLayerNamePrefix);

            props.setProperty("networkHostName", networkHostName);
            props.setProperty("networkName", networkName);
            props.setProperty("networkSubnet", networkSubnet);
            props.setProperty("networkGateway", networkGateway);
            props.setProperty("metricsPortBegin", String.valueOf(metricsPortBegin));
            props.setProperty("HostReceiverPort", String.valueOf(HostReceiverPort));
            props.setProperty("NetworkHostNodeSenderPort", String.valueOf(NetworkHostNodeSenderPort));
            props.setProperty("classNamePrefix", classNamePrefix);
            props.setProperty("cleanUpAfterSimulation", String.valueOf(cleanUpAfterSimulation));

            props.store(fos, "Simulator Configurator");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
