package org.teq.visualizer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.backend.ChartHandler;
import org.teq.configurator.SimulatorConfigurator;
import scala.Char;

import static spark.Spark.*;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
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
