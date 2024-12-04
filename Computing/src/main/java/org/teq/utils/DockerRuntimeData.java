package org.teq.utils;

import org.teq.configurator.SimulatorConfigurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DockerRuntimeData {
    private static volatile List<String> nodeNameList;
    private static final Map<String, Integer> nodeNameMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> layerNameMap = new ConcurrentHashMap<>();
    private static volatile List<String> layerList;
    private static volatile List<Integer> layerBeginList;
    private static volatile List<Integer> layerEndList;

    public static void initRuntimeData() {
        // Thread-safe initialization logic, if required
    }

    private static Path getPathByEnvironment(String path) {
        if (utils.isInDocker()) return Path.of(path);
        return Path.of(SimulatorConfigurator.hostPath + "/" + path);
    }

    public static synchronized List<String> getLayerList() {
        if (layerList != null) return layerList;

        synchronized (DockerRuntimeData.class) {
            if (layerList == null) {
                layerList = Collections.synchronizedList(new ArrayList<>());
                layerBeginList = Collections.synchronizedList(new ArrayList<>());
                layerEndList = Collections.synchronizedList(new ArrayList<>());

                Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.layerNameFileName);
                try {
                    List<String> rawLayerList = Files.readAllLines(path);
                    for (String rawLayerString : rawLayerList) {
                        if (rawLayerString.isEmpty()) break;
                        String[] result = rawLayerString.split(",");
                        layerList.add(result[0]);
                        layerBeginList.add(Integer.parseInt(result[1]));
                        layerEndList.add(Integer.parseInt(result[2]));
                    }
                    for (int i = 0; i < layerList.size(); i++) {
                        layerNameMap.put(layerList.get(i), i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return layerList;
    }

    public static List<String> getNodeNameListByLayerName(String layerName) {
        List<String> nodeNameListLayer;
        Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + layerName + "/" + SimulatorConfigurator.nodeNameFileName);
        try {
            nodeNameListLayer = Files.readAllLines(path);
            if (nodeNameListLayer.get(nodeNameListLayer.size() - 1).isEmpty())
                nodeNameListLayer.remove(nodeNameListLayer.size() - 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameListLayer;
    }

    public static synchronized List<String> getNodeNameList() {
        if (nodeNameList != null) return nodeNameList;

        synchronized (DockerRuntimeData.class) {
            if (nodeNameList == null) {
                Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.nodeNameFileName);
                try {
                    nodeNameList = Collections.synchronizedList(Files.readAllLines(path));
                    if (nodeNameList.get(nodeNameList.size() - 1).isEmpty())
                        nodeNameList.remove(nodeNameList.size() - 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                for (int i = 0; i < nodeNameList.size(); i++) {
                    nodeNameMap.put(nodeNameList.get(i), i);
                }
            }
        }

        return nodeNameList;
    }

    public static String getNodeNameById(int nodeId) {
        if (nodeId == -1) return "Sink";
        if (nodeNameList == null) getNodeNameList();
        return nodeNameList.get(nodeId);
    }

    public static int getNodeIdByName(String nodeName) {
        if (nodeNameMap.isEmpty()) getNodeNameList();
        return nodeNameMap.get(nodeName);
    }

    public static int getLayerIdByName(String layerName) {
        if (layerNameMap.isEmpty()) getLayerList();
        return layerNameMap.get(layerName);
    }

    public static String getHostIp() {
        String hostIp;
        Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.hostIpFileName);
        try {
            hostIp = Files.readAllLines(path).get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return hostIp;
    }

    public static String getNetworkHostNodeName() {
        String networkHostName;
        try (BufferedReader reader = new BufferedReader(new FileReader(
                SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.nodeNameFileName))) {
            networkHostName = reader.readLine();
            return networkHostName;
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getLayerNameByNodeName(String nodeName) {
        long nodeId = getNodeIdByName(nodeName);
        if (layerList == null) getLayerList();
        for (int i = 0; i < layerList.size(); i++) {
            if (layerBeginList.get(i) <= nodeId && nodeId <= layerEndList.get(i))
                return layerList.get(i);
        }
        return null;
    }

    /**
     * @return -1 if the node is not in that layer or the layer does not exist
     */
    public static int getNodeRankInLayer(String nodeName, String layerName) {
        List<String> nodes = getNodeNameListByLayerName(layerName);
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).equals(nodeName))
                    return i;
            }
        }
        return -1;
    }
}
