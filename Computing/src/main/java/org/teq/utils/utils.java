package org.teq.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.simulator.Simulator;

import java.io.File;
import java.io.FileOutputStream;

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
    static boolean isInDocker(){
        String inDocker = System.getenv("IN_DOCKER");
        return inDocker != null && inDocker.equals("1");
    }
}
