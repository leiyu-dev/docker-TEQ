package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.visualizer.Chart;
import org.teq.visualizer.SocketDisplayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static spark.Spark.*;

public class ChartHandler {
     private CopyOnWriteArrayList<Chart>chartList;
     private static DockerRunner dockerRunner;
     public static void setDockerRunner(DockerRunner dockerRunner1){
         dockerRunner = dockerRunner1;
     }
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

    public ChartHandler(CopyOnWriteArrayList<Chart> chartList){
        this.chartList = chartList;
    }

    public void HandleChart(){
        get("/chart", (req, res) -> {
            res.type("application/json");
            return JSON.toJSONString(chartList, SerializerFeature.DisableCircularReferenceDetect);
        });
        get("/data", (req, res) -> {
            List<ChartData> dataList = new ArrayList<>();
            for(int i = 0; i < chartList.size(); i++){
                Chart chart = chartList.get(i);
                try {
                    while(true) {
                        BlockingQueue xQueue = chart.getxAxis();
                        List<BlockingQueue> yQueueList = chart.getyAxis();
                        boolean canWrite = true;
                        if (xQueue.isEmpty()) break;
                        for (var yQueue : yQueueList) {
                            if (yQueue.isEmpty()) {
                                canWrite = false;
                                break;
                            }
                        }
                        if (canWrite) {
                            Object x = xQueue.take();
                            List<String> yList = new ArrayList<>();
                            for (var yQueue : yQueueList) {
                                Object y = yQueue.take();
                                yList.add(y.toString());
                            }
                            dataList.add(new ChartData(chart.getTitle(), x.toString(), yList));
                        }
                        else break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String dataString = JSON.toJSONString(dataList, SerializerFeature.DisableCircularReferenceDetect);
            return dataString;
        });
        post("/inspect", (req, res) -> {
            JSONObject requestBody;
            try {
                requestBody = JSON.parseObject(req.body());
            } catch (Exception e) {
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Invalid JSON format."));
            }
            if (!requestBody.containsKey("node")){
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Missing required fields: 'node'"));
            }
            String node  = requestBody.getString("node");
            BlockingQueue<Double>cpuQueue = new LinkedBlockingQueue<>();
            BlockingQueue<Double>cpuTimeQueue = new LinkedBlockingQueue<>();
            BlockingQueue<Double>memoryQueue = new LinkedBlockingQueue<>();
            BlockingQueue<Double>memoryTimeQueue = new LinkedBlockingQueue<>();
            dockerRunner.beginInspectNode(node,cpuQueue,memoryQueue,cpuTimeQueue,memoryTimeQueue);
            Chart cpuChart = new Chart(cpuTimeQueue,cpuQueue,"time/s","cpu usage/%", node, "cpu usage of " + node, "node");
            Chart memoryChart = new Chart(memoryTimeQueue, memoryQueue, "time/s", "memory usage/MB", node,  " memory usage of " + node,"node");
            chartList.add(cpuChart);
            chartList.add(memoryChart);
            List<Chart>newCharts = new ArrayList<>();
            newCharts.add(cpuChart);
            newCharts.add(memoryChart);
            res.status(200);
            res.type("application/json");
            return JSON.toJSONString(newCharts);
        });
    }
}
