package org.teq.visualizer;

import akka.japi.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class FileDisplayer extends MetricsDisplayer{
    private static final Logger logger = LogManager.getLogger(MetricsDisplayer.class);
    List<Chart> chartList = new ArrayList<>();
    List<Pair<File,FileOutputStream>> fileOutputStreamList = new ArrayList<>();
    @Override
    public void addChart(Chart chart){
        chartList.add(chart);
    }

    /**
     * write all Chart statistics to different files
     */
    @Override
    public void display(){
        for(Chart chart: chartList){
            String fileName = SimulatorConfig.projectPath + "/statistics/" + chart.getTitle() + ".txt";
            File file = new File(fileName);
            File nodeNameFile = new File(fileName);
            File parentDir = nodeNameFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try {
                FileOutputStream fos = new FileOutputStream(nodeNameFile,false);
                fileOutputStreamList.add(new Pair<>(file,fos));
                logger.info("Create file: " + file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                logger.error("Failed to create file: " + file.getAbsolutePath());
                throw new RuntimeException(e);
            }
        }
        logger.info("Start to write statistics to files");
        Thread thread = new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(SimulatorConfig.displayInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < chartList.size(); i++){
                    Chart chart = chartList.get(i);
                    try {
                        FileOutputStream fos = fileOutputStreamList.get(i).second();
                        BlockingQueue xQueue = chart.getxAxis();
                        List<BlockingQueue> yQueueList = chart.getyAxis();
                        boolean canWrite = true;
                        if (xQueue.isEmpty())continue;
                        for(var yQueue : yQueueList) {
                            if(yQueue.isEmpty()){
                                canWrite = false;
                                break;
                            }
                        }
                        if(canWrite){
                            Object x = xQueue.take();
                            StringBuilder context = new StringBuilder(x.toString() + ",");
                            for(var yQueue : yQueueList) {
                                Object y = yQueue.take();
                                context.append(y).append(",");
                            }
                            //logger.info(context + " into " + fileOutputStreamList.get(i).first().getName());
                            fos.write((context + "\n").getBytes());
                            fos.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
