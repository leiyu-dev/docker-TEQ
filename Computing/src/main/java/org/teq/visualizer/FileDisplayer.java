package org.teq.visualizer;

import akka.japi.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.utils.connector.CommonDataReceiver;

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
            String fileName = SimulatorConfigurator.projectPath + "/statistics/" + chart.getTitle() + ".txt";
            File file = new File(fileName);
            File nodeNameFile = new File(fileName);
            File parentDir = nodeNameFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try {
                FileOutputStream fos = new FileOutputStream(nodeNameFile);
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
                    Thread.sleep(SimulatorConfigurator.displayInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for(int i = 0; i < chartList.size(); i++){
                    Chart chart = chartList.get(i);
                    try {
                        FileOutputStream fos = fileOutputStreamList.get(i).second();
                        BlockingQueue xQueue = chart.getxAxis();
                        BlockingQueue yQueue = chart.getyAxis();
                        if(xQueue.isEmpty() || yQueue.isEmpty()){
                            if(xQueue.isEmpty())logger.info("try to display " + chart.getTitle() + " but xQueue has no data");
                            if(yQueue.isEmpty())logger.info("try to display " + chart.getTitle() + " but yQueue has no data");
                            continue;
                        }
                        Object x = xQueue.take();
                        Object y = yQueue.take();
                        String context = x.toString() + "," + y.toString() + "\n";
                        logger.info(context);
                        fos.write(context.getBytes());
                        fos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
