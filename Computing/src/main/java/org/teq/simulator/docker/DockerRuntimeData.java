package org.teq.simulator.docker;

import org.teq.configurator.DockerConfigurator;

import javax.print.Doc;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DockerRuntimeData {
    private List<String> nodeNameList;
    public DockerRuntimeData() {
        Path path = Paths.get(DockerConfigurator.dataFolder+ "/" + DockerConfigurator.nodeNameFilePath);
        try {
            nodeNameList = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getNodeNameList() {
        return nodeNameList;
    }
}
