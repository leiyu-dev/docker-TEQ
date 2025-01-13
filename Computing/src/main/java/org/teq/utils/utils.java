package org.teq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class utils {
    private static final Logger logger = LogManager.getLogger(utils.class);
    /* write content to file , if the dir does not exist, create it */
    static public void writeStringToFile(String filename, String content) {
        File nodeNameFile = new File(filename);
        File parentDir = nodeNameFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("Failed to create directories: " + parentDir);
            }
        }
        try{
            FileOutputStream fos = new FileOutputStream(nodeNameFile);
                fos.write(content.getBytes());
        } catch (Exception e){
            logger.error("Error in writing string into file " + filename);
        }
    }
    static public String readFirstLineFromFile(String filename) {
        Path path = Paths.get(filename);
        try {
            return Files.readAllLines(path).get(0);
        } catch (Exception e) {
            logger.error("Error in reading string from file " + filename);
            return null;
        }
    }
    static public List<String> readLinesFromFile(String filename) {
        Path path = Paths.get(filename);
        try {
            return Files.readAllLines(path);
        } catch (Exception e) {
            logger.error("Error in reading lines from file " + filename);
            return null;
        }
    }

    static boolean isInDocker(){
        String inDocker = System.getenv("IN_DOCKER");
        return inDocker != null && inDocker.equals("1");
    }
}
