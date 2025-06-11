package org.teq.visualizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.backend.ChartHandler;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketDisplayer extends MetricsDisplayer{
    private static final Logger logger = LogManager.getLogger(SocketDisplayer.class);

    CopyOnWriteArrayList<Chart> chartList = new CopyOnWriteArrayList<>();
    @Override
    public void addChart(Chart chart) {
        chartList.add(chart);
    }

    @Override
    public void display() {
        ChartHandler chartHandler = new ChartHandler(chartList);
        chartHandler.HandleChart();
    }
}
