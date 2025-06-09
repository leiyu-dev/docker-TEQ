package org.teq.backend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
     private static final Logger logger = LogManager.getLogger(ChartHandler.class);
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
            logger.debug("API Request: GET /chart - Retrieving chart list");
            res.type("application/json");
            String result = JSON.toJSONString(chartList, SerializerFeature.DisableCircularReferenceDetect);
            logger.debug("API Response: GET /chart - Returned {} charts", chartList.size());
            return result;
        });
        get("/data", (req, res) -> {
            logger.trace("API Request: GET /data - Retrieving chart data");
            List<ChartData> dataList = new ArrayList<>();
            try {
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
                        logger.error("API Error: GET /data - Error processing chart: {}", chart.getTitle(), e);
                        e.printStackTrace();
                    }
                }
                String dataString = JSON.toJSONString(dataList, SerializerFeature.DisableCircularReferenceDetect);
                logger.trace("API Response: GET /data - Returned {} data points", dataList.size());
                return dataString;
            } catch (Exception e) {
                logger.error("API Error: GET /data - Failed to retrieve chart data", e);
                throw e;
            }
        });
        post("/inspect", (req, res) -> {
            logger.info("API Request: POST /inspect - Starting node inspection");
            JSONObject requestBody;
            try {
                requestBody = JSON.parseObject(req.body());
                logger.debug("API Request: POST /inspect - Request body: {}", req.body());
            } catch (Exception e) {
                logger.error("API Error: POST /inspect - Invalid JSON format in request body", e);
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Invalid JSON format."));
            }
            if (!requestBody.containsKey("node")){
                logger.warn("API Warning: POST /inspect - Missing required field: node");
                res.status(400); // Bad Request
                return JSON.toJSONString(Map.of("error", "Missing required fields: 'node'"));
            }
            String node  = requestBody.getString("node");
            logger.info("API Processing: POST /inspect - Beginning inspection for node: {}", node);
            try {
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
                logger.info("API Success: POST /inspect - Successfully started inspection for node: {}", node);
                return JSON.toJSONString(newCharts);
            } catch (Exception e) {
                logger.error("API Error: POST /inspect - Failed to start inspection for node: {}", node, e);
                res.status(500);
                return JSON.toJSONString(Map.of("error", "Failed to start node inspection: " + e.getMessage()));
            }
        });
    }
}
