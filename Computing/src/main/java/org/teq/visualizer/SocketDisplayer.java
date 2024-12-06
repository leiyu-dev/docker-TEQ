package org.teq.visualizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfigurator;

import static spark.Spark.*;
import java.util.HashMap;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SocketDisplayer extends MetricsDisplayer{
    private static final Logger logger = LogManager.getLogger(SocketDisplayer.class);
    class ChartData{
        String chartName;
        String xData;
        String yData;
        public ChartData(String chartName, String xData, String yData){
            this.chartName = chartName;
            this.xData = xData;
            this.yData = yData;
        }
        //to json
        public String toString(){
            return "{\"chartName\":\"" + chartName + "\",\"xData\":\"" + xData + "\",\"yData\":\"" + yData + "\"}";
        }
    }
    List<Chart> chartList = new ArrayList<>();
    @Override
    public void addChart(Chart chart) {
        chartList.add(chart);
    }



    @Override
    public void display() {
        port(SimulatorConfigurator.restfulPort);
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        get("/chart", (req, res) -> {
            res.type("application/json");
            logger.error("get chart!!");
            String chart = "[";
            for (Chart c : chartList) {
                chart += c.toString() + ",";
            }
            chart += "]";
            return chart;
        });
        get("/data", (req, res) -> {
            List<ChartData>dataList = new ArrayList<>();
            for(int i = 0; i < chartList.size(); i++){
                Chart chart = chartList.get(i);
                try {
                    BlockingQueue xQueue = chart.getxAxis();
                    BlockingQueue yQueue = chart.getyAxis();
                    while(true) {
                        if (xQueue.isEmpty() || yQueue.isEmpty()) {
//                            if(xQueue.isEmpty())logger.info("try to display " + chart.getTitle() + " but xQueue has no data");
//                            if(yQueue.isEmpty())logger.info("try to display " + chart.getTitle() + " but yQueue has no data");
                            break;
                        }
                        Object x = xQueue.take();
                        Object y = yQueue.take();
                        dataList.add(new ChartData(chart.getTitle(), x.toString(), y.toString()));
//                        logger.info(context + " into " + fileOutputStreamList.get(i).first().getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String dataString = "[";
            for(ChartData data : dataList){
                dataString += data.toString() + ",";
            }
            dataString += "]";
            return dataString;
        });
    }
}
