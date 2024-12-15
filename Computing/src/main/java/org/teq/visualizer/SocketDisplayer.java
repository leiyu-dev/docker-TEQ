package org.teq.visualizer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfigurator;

import static spark.Spark.*;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SocketDisplayer extends MetricsDisplayer{
    private static final Logger logger = LogManager.getLogger(SocketDisplayer.class);

    class ChartData implements Serializable {
        private String chartName;
        private String xData;
        private List<String> yData;
        public ChartData(String chartName, String xData, List<String> yData){
            this.setChartName(chartName);
            this.setxData(xData);
            this.setyData(yData);
        }


        public String getChartName() {
            return chartName;
        }

        public void setChartName(String chartName) {
            this.chartName = chartName;
        }

        public String getxData() {
            return xData;
        }

        public void setxData(String xData) {
            this.xData = xData;
        }

        public List<String> getyData() {
            return yData;
        }

        public void setyData(List<String> yData) {
            this.yData = yData;
        }
    }
    List<Chart> chartList = new ArrayList<>();
    @Override
    public void addChart(Chart chart) {
        chartList.add(chart);
    }



    @Override
    public void display() {
//        logger.error(JSON.toJSONString(chartList));
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
            return JSON.toJSONString(chartList, SerializerFeature.DisableCircularReferenceDetect);
        });
        get("/data", (req, res) -> {
            List<ChartData>dataList = new ArrayList<>();
            for(int i = 0; i < chartList.size(); i++){
                Chart chart = chartList.get(i);
                try {
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
                        List<String>yList = new ArrayList<>();
                        for(var yQueue : yQueueList) {
                            Object y = yQueue.take();
                            yList.add(y.toString());
                        }
                        dataList.add(new ChartData(chart.getTitle(), x.toString(), yList));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String dataString = JSON.toJSONString(dataList, SerializerFeature.DisableCircularReferenceDetect);
            return dataString;
        });
    }
}
