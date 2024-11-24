package org.teq.configurator;

public class NetworkConfigurator {
    /**
     *   WARNING: the networkHostName is only used in Simulator to generate the true networkHostNodeName,
     *   do not use it as host name in DataSender
     */
    public static String networkHostName = "NetworkHost";
    public static String networkName = "teq-network";
    public static String networkSubnet = "10.0.0.0/16";
    public static String networkGateway = "10.0.0.2";
    public static int metricsPortBegin = 9100;
    public static int HostReceiverPort = 8888;
    public static int NetworkHostNodeSenderPort = 8888;
}