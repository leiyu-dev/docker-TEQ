package org.teq.simulator.docker;

import org.teq.configurator.DockerConfigurator;
import org.teq.layer.Layer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerRuntimeData {
    static private List<String> nodeNameList;
    static private Map<String,Long> nodeNameMap;

    static private List<String> layerList;
    private static int i;

    public DockerRuntimeData(){};
    static public List<String> getLayerList(){
        if(layerList != null) return layerList;
        layerList = new ArrayList<>();
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + DockerConfigurator.layerNameFileName);
        try {
            layerList = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return layerList;
    }
    static public List<String> getNodeNameListByLayerName(String layerName){
        List<String> nodeNameListLayer;
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + layerName + "/" + DockerConfigurator.nodeNameFileName);
        try {
            nodeNameListLayer = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameListLayer;
    }
    static public List<String> getNodeNameList() {
        if(nodeNameList != null)return nodeNameList;
        nodeNameList = new ArrayList<>();
        nodeNameMap = new HashMap<>();
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + DockerConfigurator.nodeNameFileName);
        try {
            nodeNameList = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        for(int i=0 ; i<nodeNameList.size() ; i++){
            nodeNameMap.put(nodeNameList.get(i), (long) i);
        }
        return nodeNameList;
    }
    static public String getNodeNameById(int nodeId){
        if(nodeNameList == null)getNodeNameList();
        return nodeNameList.get(nodeId);
    }
    static public long getNodeIdByName(String nodeName){
        if(nodeNameMap == null)getNodeNameList();
        return nodeNameMap.get(nodeName);
    }
    static public String getHostIp(){
        String hostIp;
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + DockerConfigurator.hostIpFileName);
        try {
            hostIp = Files.readAllLines(path).get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return hostIp;
    }
    static public String getNetworkHostNodeName(){
        String networkHostName;
        try (BufferedReader reader = new BufferedReader(new FileReader(
                DockerConfigurator.dataFolderName + "/" + DockerConfigurator.nodeNameFileName))) {
            networkHostName = reader.readLine();
            return networkHostName;
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
