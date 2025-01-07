package org.teq.mearsurer.receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.ILoggerFactory;
import org.teq.simulator.Simulator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.visualizer.Chart;
import org.teq.visualizer.MetricsDisplayer;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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
            cpuUsageQueueList.add(new ArrayBlockingQueue<>(100));
            memoryUsageQueueList.add(new ArrayBlockingQueue<>(100));
        }

        List<BlockingQueue<Double>> cpuUsageLayerList = new ArrayList<>();//for each layer
        List<BlockingQueue<Double>> memoryUsageLayerList = new ArrayList<>();
        for (int i = 0; i < layerList.size(); i++) {
            cpuUsageLayerList.add(new ArrayBlockingQueue<>(100));
            memoryUsageLayerList.add(new ArrayBlockingQueue<>(100));
        }

        DockerRunner dockerRunner = simulator.getDockerRunner();
        try {
            dockerRunner.beginDockerMetricsCollection(cpuUsageQueueList, memoryUsageQueueList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        metricsDisplayer.addChart(new Chart(TimeQueueGenerator.getTimeQueue(3000), cpuUsageLayerList, "time/s", "cpu usage/%",DockerRuntimeData.getLayerList(), " cpu usage"));
        metricsDisplayer.addChart(new Chart(TimeQueueGenerator.getTimeQueue(3000), memoryUsageLayerList, "time/s", "memory usage/MB",DockerRuntimeData.getLayerList(),  " memory usage"));


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
//                        logger.info("try to take from queue");
                            double cpuUsage = (cpuUsageQueueList.get(i).take());
                            double memoryUsage = (memoryUsageQueueList.get(i).take());
//                        logger.info("take from queue");
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

                for (int i = 0; i < layerList.size(); i++) {
                    if (nodeCountList.get(i) == 0) {
                        continue;
                    }
                    try {
//                        logger.info("for layer " + layerList.get(i) + "add cpu: " + cpuUsageLayerListSum.get(i) / nodeCountList.get(i) + " memory: " + memoryUsageLayerListSum.get(i) / nodeCountList.get(i));
                        cpuUsageLayerList.get(i).put(cpuUsageLayerListSum.get(i) / nodeCountList.get(i));
                        memoryUsageLayerList.get(i).put(memoryUsageLayerListSum.get(i) / nodeCountList.get(i) * 1024.0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }
}
