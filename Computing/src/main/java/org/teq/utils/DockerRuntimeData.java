package org.teq.utils;

import com.alibaba.fastjson2.JSON;
import org.teq.configurator.SimulatorConfig;
import org.teq.node.DockerNodeParameters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DockerRuntimeData provides utility methods for managing and accessing Docker runtime data
 * including node information, layer configuration, and network parameters.
 * This class uses singleton pattern with thread-safe lazy initialization.
 */
public class DockerRuntimeData {
    /** Cached list of all node names in the system */
    private static volatile List<String> nodeNameList;
    
    /** Map for quick lookup of node index by node name */
    private static final Map<String, Integer> nodeNameMap = new ConcurrentHashMap<>();
    
    /** Map for quick lookup of layer index by layer name */
    private static final Map<String, Integer> layerNameMap = new ConcurrentHashMap<>();
    
    /** Cached list of all layer names in the system */
    private static volatile List<String> layerList;
    
    /** Cached list of begin indices for each layer */
    private static volatile List<Integer> layerBeginList;
    
    /** Cached list of end indices for each layer */
    private static volatile List<Integer> layerEndList;

    /** Cached list of Docker node parameters for all nodes */
    public static volatile List<DockerNodeParameters> nodeParametersList;

    /**
     * Initialize runtime data if needed. Currently a placeholder for future initialization logic.
     */
    public static void initRuntimeData() {
        // Thread-safe initialization logic, if required
    }

    /**
     * Get the appropriate file path based on whether the application is running inside Docker.
     * 
     * @param path the relative path
     * @return the complete path based on execution environment
     */
    private static Path getPathByEnvironment(String path) {
        if (utils.isInDocker()) return Path.of(path);
        return Path.of(SimulatorConfig.hostPath + "/" + path);
    }

    /**
     * Get the list of Docker node parameters with thread-safe lazy initialization.
     * 
     * @return list of DockerNodeParameters, null if error occurs
     */
    public static synchronized List<DockerNodeParameters> getNodeParametersList() {
        if(nodeParametersList != null) return nodeParametersList;

        synchronized (DockerRuntimeData.class){
            if(nodeParametersList == null){
                nodeParametersList = Collections.synchronizedList(new ArrayList<>());
                Path path = getPathByEnvironment(SimulatorConfig.dataFolderName + "/nodeParams" );
                try {
                    // Read node parameters from file and parse JSON
                    String parametersString = Files.readAllLines(path).get(0);
                    nodeParametersList = JSON.parseArray(parametersString, DockerNodeParameters.class);
                }
                catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return nodeParametersList;
    }

    /**
     * Get the list of layer names with thread-safe lazy initialization.
     * Also initializes layer begin/end lists and layer name mapping.
     * 
     * @return list of layer names, null if error occurs
     */
    public static synchronized List<String> getLayerList() {
        if (layerList != null) return layerList;

        synchronized (DockerRuntimeData.class) {
            if (layerList == null) {
                layerList = Collections.synchronizedList(new ArrayList<>());
                layerBeginList = Collections.synchronizedList(new ArrayList<>());
                layerEndList = Collections.synchronizedList(new ArrayList<>());

                Path path = getPathByEnvironment(SimulatorConfig.dataFolderName + "/" + SimulatorConfig.layerNameFileName);
                try {
                    List<String> rawLayerList = Files.readAllLines(path);
                    // Parse each line: layerName,beginIndex,endIndex
                    for (String rawLayerString : rawLayerList) {
                        if (rawLayerString.isEmpty()) break;
                        String[] result = rawLayerString.split(",");
                        layerList.add(result[0]);
                        layerBeginList.add(Integer.parseInt(result[1]));
                        layerEndList.add(Integer.parseInt(result[2]));
                    }
                    // Build layer name to index mapping
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

    /**
     * Get the list of node names for a specific layer.
     * 
     * @param layerName the name of the layer
     * @return list of node names in the specified layer, null if error occurs
     */
    public static List<String> getNodeNameListByLayerName(String layerName) {
        List<String> nodeNameListLayer;
        Path path = getPathByEnvironment(SimulatorConfig.dataFolderName + "/" + layerName + "/" + SimulatorConfig.nodeNameFileName);
        try {
            nodeNameListLayer = Files.readAllLines(path);
            // Remove empty last line if exists
            if (nodeNameListLayer.get(nodeNameListLayer.size() - 1).isEmpty())
                nodeNameListLayer.remove(nodeNameListLayer.size() - 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameListLayer;
    }

    /**
     * Get the complete list of node names with thread-safe lazy initialization.
     * Also builds the node name to index mapping.
     * 
     * @return list of all node names, null if error occurs
     */
    public static synchronized List<String> getNodeNameList() {
        if (nodeNameList != null) return nodeNameList;

        synchronized (DockerRuntimeData.class) {
            if (nodeNameList == null) {
                Path path = getPathByEnvironment(SimulatorConfig.dataFolderName + "/" + SimulatorConfig.nodeNameFileName);
                try {
                    nodeNameList = Collections.synchronizedList(Files.readAllLines(path));
                    // Remove empty last line if exists
                    if (nodeNameList.get(nodeNameList.size() - 1).isEmpty())
                        nodeNameList.remove(nodeNameList.size() - 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                // Build node name to index mapping
                for (int i = 0; i < nodeNameList.size(); i++) {
                    nodeNameMap.put(nodeNameList.get(i), i);
                }
            }
        }

        return nodeNameList;
    }

    /**
     * Get node name by its ID.
     * 
     * @param nodeId the node ID (-1 represents Sink node)
     * @return the node name corresponding to the ID
     */
    public static String getNodeNameById(int nodeId) {
        if (nodeId == -1) return "Sink";
        if (nodeNameList == null) getNodeNameList();
        return nodeNameList.get(nodeId);
    }

    /**
     * Get node ID by its name.
     * 
     * @param nodeName the node name
     * @return the node ID corresponding to the name
     */
    public static int getNodeIdByName(String nodeName) {
        if (nodeNameMap.isEmpty()) getNodeNameList();
        return nodeNameMap.get(nodeName);
    }

    /**
     * Get layer ID by its name.
     * 
     * @param layerName the layer name
     * @return the layer ID corresponding to the name
     */
    public static int getLayerIdByName(String layerName) {
        if (layerNameMap.isEmpty()) getLayerList();
        return layerNameMap.get(layerName);
    }

    /**
     * Get the host IP address from configuration file.
     * 
     * @return the host IP address, null if error occurs
     */
    public static String getHostIp() {
        String hostIp;
        Path path = getPathByEnvironment(SimulatorConfig.dataFolderName + "/" + SimulatorConfig.hostIpFileName);
        try {
            hostIp = Files.readAllLines(path).get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return hostIp;
    }

    /**
     * Get the network host node name (first node in the node list).
     * 
     * @return the network host node name, null if error occurs
     */
    public static String getNetworkHostNodeName() {
        String networkHostName;
        try (BufferedReader reader = new BufferedReader(new FileReader(
                SimulatorConfig.dataFolderName + "/" + SimulatorConfig.nodeNameFileName))) {
            networkHostName = reader.readLine();
            return networkHostName;
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the layer name that contains the specified node.
     * 
     * @param nodeName the node name
     * @return the layer name containing the node, null if not found
     */
    public static String getLayerNameByNodeName(String nodeName) {
        long nodeId = getNodeIdByName(nodeName);
        if (layerList == null) getLayerList();
        // Check which layer contains this node ID
        for (int i = 0; i < layerList.size(); i++) {
            if (layerBeginList.get(i) <= nodeId && nodeId <= layerEndList.get(i))
                return layerList.get(i);
        }
        return null;
    }

    /**
     * Get the rank (position) of a node within its layer.
     * 
     * @param nodeName the node name
     * @param layerName the layer name
     * @return the rank of the node in the layer, -1 if not found or layer doesn't exist
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

    /**
     * Get Docker node parameters for a specific node by name.
     * 
     * @param nodeName the node name
     * @return the DockerNodeParameters for the specified node
     */
    public static DockerNodeParameters getNodeParametersByNodeName(String nodeName){
        List<DockerNodeParameters> nodeParametersList = getNodeParametersList();
        return nodeParametersList.get(getNodeIdByName(nodeName));
    }

    /**
     * Get Docker node parameters for a specific node by ID.
     * 
     * @param nodeId the node ID
     * @return the DockerNodeParameters for the specified node
     */
    public static DockerNodeParameters getNodeParametersByNodeId(int nodeId){
        List<DockerNodeParameters> nodeParametersList = getNodeParametersList();
        return nodeParametersList.get(nodeId);
    }
}
