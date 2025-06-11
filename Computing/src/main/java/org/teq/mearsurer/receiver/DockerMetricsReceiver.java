package org.teq.mearsurer.receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.MetricsConfig;
import org.teq.simulator.Simulator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.visualizer.TimeChart;
import org.teq.visualizer.MetricsDisplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DockerMetricsReceiver extends AbstractReceiver implements Runnable{
    private static final Logger logger = LogManager.getLogger(DockerMetricsReceiver.class);

    Simulator simulator;
    public DockerMetricsReceiver(MetricsDisplayer metricsDisplayer,Simulator simulator) {
        super(metricsDisplayer);
        this.simulator = simulator;
    }

    @Override
    public void beginReceive(){
        Thread thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        List<String> nodeList = DockerRuntimeData.getNodeNameList();
        List<String>layerList = DockerRuntimeData.getLayerList();

        List<BlockingQueue<Double>> cpuUsageQueueList = new ArrayList<>();
        List<BlockingQueue<Double>> memoryUsageQueueList = new ArrayList<>();

        for (int i = 0; i < nodeList.size(); i++) {
            cpuUsageQueueList.add(new LinkedBlockingQueue<>());
            memoryUsageQueueList.add(new LinkedBlockingQueue<>());
        }

        // TimeChart will manage the Y-axis queues internally, no need to create them manually

        DockerRunner dockerRunner = simulator.getDockerRunner();
        try {
            dockerRunner.beginDockerMetricsCollection(cpuUsageQueueList, memoryUsageQueueList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TimeChart<Double> cpuTimeChart = new TimeChart<>("cpu usage/%", DockerRuntimeData.getLayerList(), " cpu usage", "overview");
        TimeChart<Double> memoryTimeChart = new TimeChart<>("memory usage/MB", DockerRuntimeData.getLayerList(), " memory usage", "overview");
        
        metricsDisplayer.addChart(cpuTimeChart);
        metricsDisplayer.addChart(memoryTimeChart);


        Thread thread = new Thread(() ->{
            List<Double>nodeCountList = new ArrayList<>();
            List<Double>cpuUsageLayerListSum = new ArrayList<>();
            List<Double>memoryUsageLayerListSum = new ArrayList<>();
            for (int i = 0; i < layerList.size(); i++) {
                nodeCountList.add(0.0);
                cpuUsageLayerListSum.add(0.0);
                memoryUsageLayerListSum.add(0.0);
            }
            while (true) {
                //set to 0
                for (int i = 0; i < layerList.size(); i++) {
                    nodeCountList.set(i, 0.0);
                    cpuUsageLayerListSum.set(i, 0.0);
                    memoryUsageLayerListSum.set(i, 0.0);
                }
                for (int i = 0; i < nodeList.size(); i++) {
                    try {
                        while(!cpuUsageQueueList.get(i).isEmpty()) {
                            double cpuUsage = (cpuUsageQueueList.get(i).take());
                            double memoryUsage = (memoryUsageQueueList.get(i).take());
                            String nodeName = nodeList.get(i);
                            String layerName = DockerRuntimeData.getLayerNameByNodeName(nodeName);
                            if (layerName == null) { // network node or other user defined node
                                continue;
                            }
                            int layerIndex = DockerRuntimeData.getLayerIdByName(layerName);
                            nodeCountList.set(layerIndex, nodeCountList.get(layerIndex) + 1);
                            cpuUsageLayerListSum.set(layerIndex, cpuUsageLayerListSum.get(layerIndex) + cpuUsage);
                            memoryUsageLayerListSum.set(layerIndex, memoryUsageLayerListSum.get(layerIndex) + memoryUsage);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                // prepare CPU and memory usage data
                List<Double> cpuUsageData = new ArrayList<>();
                List<Double> memoryUsageData = new ArrayList<>();
                
                boolean hasData = false;
                for (int i = 0; i < layerList.size(); i++) {
                    if (nodeCountList.get(i) == 0) {
                        cpuUsageData.add(0.0);
                        memoryUsageData.add(0.0);
                    } else {
                        cpuUsageData.add(cpuUsageLayerListSum.get(i) / nodeCountList.get(i));
                        memoryUsageData.add(memoryUsageLayerListSum.get(i) / nodeCountList.get(i) * 1024.0);
                        hasData = true;
                    }
                }
                
                // only add to chart when there is data
                if (hasData) {
                    cpuTimeChart.addDataPoint(cpuUsageData);
                    memoryTimeChart.addDataPoint(memoryUsageData);
                }
                try {
                    Thread.sleep(MetricsConfig.dockerMetricsDisplayInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }
}
