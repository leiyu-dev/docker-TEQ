package org.teq.simulator.docker;

import org.teq.configurator.DockerConfigurator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DockerRuntimeData {
    public DockerRuntimeData(){};
    static public List<String> getLayerList(){
        List<String> layerList;
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
        List<String> nodeNameList;
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + layerName + "/" + DockerConfigurator.nodeNameFileName);
        try {
            nodeNameList = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameList;
    }
    static public List<String> getNodeNameList() {
        List<String> nodeNameList;
        Path path = Paths.get(DockerConfigurator.dataFolderName + "/" + DockerConfigurator.nodeNameFileName);
        try {
            nodeNameList = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return nodeNameList;
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
