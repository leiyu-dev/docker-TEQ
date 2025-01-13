package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.visualizer.Chart;
import org.teq.visualizer.SocketDisplayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static spark.Spark.*;

public class ChartHandler {
     class ChartData  {
        private String chartName;
        private String xData;
        private List<String> yData;

        public ChartData(String chartName, String xData, List<String> yData) {
            this.chartName = chartName;
            this.xData = xData;
            this.yData = yData;
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
     public void HandleChart(List<Chart>chartList){
        get("/chart", (req, res) -> {
            res.type("application/json");
            return JSON.toJSONString(chartList, SerializerFeature.DisableCircularReferenceDetect);
        });
        get("/data", (req, res) -> {
            List<ChartData> dataList = new ArrayList<>();
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
