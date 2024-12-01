package org.teq.utils;

import org.teq.configurator.SimulatorConfigurator;

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
    static private Map<String,Integer> nodeNameMap;

    static private List<String> layerList;
    static private List<Integer>layerBeginList;
    static private List<Integer>layerEndList;

    static public void initRuntimeData(){

    }
    static Path getPathByEnvironment(String path){
        if(utils.isInDocker())return Path.of(path);
        return Path.of(SimulatorConfigurator.hostPath + "/" + path);
    }
    static public List<String> getLayerList(){
        if(layerList != null) return layerList;
        layerList = new ArrayList<>();
        layerBeginList = new ArrayList<>();
        layerEndList = new ArrayList<>();
        Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.layerNameFileName);
        try {
            List<String>rawLayerList = Files.readAllLines(path);
            for(String rawLayerString : rawLayerList){
                if(rawLayerString.isEmpty())break;
                String[] result = rawLayerString.split(",");
                layerList.add(result[0]);
                layerBeginList.add(Integer.parseInt(result[1]));
                layerEndList.add(Integer.parseInt(result[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return layerList;
    }
    static public List<String> getNodeNameListByLayerName(String layerName){
        List<String> nodeNameListLayer;
        Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + layerName + "/" + SimulatorConfigurator.nodeNameFileName);
        try {
            nodeNameListLayer = Files.readAllLines(path);
            if(nodeNameListLayer.get(nodeNameListLayer.size()-1).isEmpty())
                nodeNameListLayer.remove(nodeNameListLayer.size()-1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameListLayer;
    }
    static public List<String> getNodeNameList() {
        if(nodeNameList != null)return nodeNameList;
        nodeNameMap = new HashMap<>();
        Path path = getPathByEnvironment(SimulatorConfigurator.dataFolderName + "/" + SimulatorConfigurator.nodeNameFileName);
        try {
            nodeNameList = Files.readAllLines(path);
            if(nodeNameList.get(nodeNameList.size()-1).isEmpty())
                nodeNameList.remove(nodeNameList.size()-1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        for(int i=0 ; i<nodeNameList.size() ; i++){
            nodeNameMap.put(nodeNameList.get(i),  i);
        }
        return nodeNameList;
    }
    static public String getNodeNameById(int nodeId){
        if(nodeId == -1)return "Sink";
        if(nodeNameList == null)getNodeNameList();
        return nodeNameList.get(nodeId);
    }
    static public int getNodeIdByName(String nodeName){
        if(nodeNameMap == null)getNodeNameList();
        return nodeNameMap.get(nodeName);
    }
    static public String getHostIp(){
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
    static public String getNetworkHostNodeName(){
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
    static public String getLayerNameByNodeName(String nodeName){
        long nodeId = getNodeIdByName(nodeName);
        if(layerList==null)getLayerList();
        for(int i=0; i<layerList.size(); i++){
            if(layerBeginList.get(i)<=nodeId && nodeId<=layerEndList.get(i))
                return layerList.get(i);
        }
        return null;
    }

    /**
     *  @return -1 if the node is not in that layer or the layer does not exist
     */
    static public int getNodeRankInLayer(String nodeName,String layerName){
        List<String>nodes = getNodeNameListByLayerName(layerName);
        if (nodes != null) {
            for(int i=0; i<nodes.size(); i++){
                if(nodes.get(i).equals(nodeName))
                    return i;
            }
        }
        return -1;
    }
}
