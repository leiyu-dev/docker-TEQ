package org.teq.visualizer;

import org.teq.configurator.SimulatorConfigurator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDisplayer extends MetricsDisplayer{
    List<Chart> chartList = new ArrayList<>();
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
            String fileName = "statistics/" + chart.getTitle() + ".txt";
            File file = new File(fileName);

        }
    }

}
