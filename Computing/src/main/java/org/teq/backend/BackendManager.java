package org.teq.backend;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfigurator;
import org.teq.simulator.Simulator;


import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.teq.utils.utils;

import static spark.Spark.*;

public class BackendManager {
    private static final Logger logger = LogManager.getLogger(BackendManager.class);
    Simulator simulator;
    public class Status {
        private String status;
        private int layerCount;
        private int nodeCount;
        private int algorithmCount;

        private String cpuUsage;
        private String memoryUsage;
        private String upTime;

        public Status(String status, int layerCount, int nodeCount, int algorithmCount, String cpuUsage, String memoryUsage, String upTime) {
            this.status = status;
            this.layerCount = layerCount;
            this.nodeCount = nodeCount;
            this.algorithmCount = algorithmCount;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.upTime = upTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getLayerCount() {
            return layerCount;
        }

        public void setLayerCount(int layerCount) {
            this.layerCount = layerCount;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
        }

        public int getAlgorithmCount() {
            return algorithmCount;
        }

        public void setAlgorithmCount(int algorithmCount) {
            this.algorithmCount = algorithmCount;
        }

        public String getCpuUsage() {
            return cpuUsage;
        }

        public void setCpuUsage(String cpuUsage) {
            this.cpuUsage = cpuUsage;
        }

        public String getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(String memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        public String getUpTime() {
            return upTime;
        }

        public void setUpTime(String upTime) {
            this.upTime = upTime;
        }
    }
    public BackendManager(Simulator simulator) {
        this.simulator = simulator;
    }
    public void launch() {
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

        get("/app/status", (req, res) -> {
            int cores = Runtime.getRuntime().availableProcessors();
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double systemCpuLoad = osBean.getSystemCpuLoad();
            long physicalTotalMemory = osBean.getTotalPhysicalMemorySize();
            long physicalFreeMemory = osBean.getFreePhysicalMemorySize();
            long startTime = utils.getStartTime();
            long upTime = (System.currentTimeMillis() - startTime) / 1000;
            Status status = new Status(simulator.getState(), simulator.getLayerCount(), simulator.getNodeCount(), simulator.getAlgorithmCount(),
                    String.format("%.2f", systemCpuLoad * 100) + "% / " + cores + " cores",
                    String.format("%.2f", (physicalTotalMemory - physicalFreeMemory) / 1024.0 / 1024.0 / 1024.0 ) + " GB (buffered) / " +
                            String.format("%.2f", physicalTotalMemory / 1024.0 / 1024.0 / 1024.0) + " GB",
                    upTime/60 + "min" + upTime%60 + "s");
            String jsonString = JSON.toJSONString(status);
            return jsonString;
        });


        GlobalConfigHandler globalConfigHandler = new GlobalConfigHandler(simulator.getConfigs());
        globalConfigHandler.handleGlobalConfig();
        NodeConfigHandler nodeConfigHandler = new NodeConfigHandler(simulator.getParameters(),simulator.getDockerRunner());
        nodeConfigHandler.handleNodeConfig();
        ChartHandler.setDockerRunner(simulator.getDockerRunner());
        ControlHandler controlHandler = new ControlHandler(simulator);
        controlHandler.handleControl();
    }
}
