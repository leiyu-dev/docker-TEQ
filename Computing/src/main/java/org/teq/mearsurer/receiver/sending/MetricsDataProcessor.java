package org.teq.mearsurer.receiver.sending;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.utils.DockerRuntimeData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics data processor (Static class)
 * Responsible for processing and calculating various metrics
 */
public class MetricsDataProcessor {
    
    /** Time unit conversion: nanoseconds to milliseconds */
    private static final double NANO_TO_MILLI = 1000.0 * 1000.0;
    
    private static final Logger logger = LogManager.getLogger(MetricsDataProcessor.class);
    
    // Static data queues
    private static BlockingQueue<Double> rawOverallProcessingLatencyQueue;
    private static BlockingQueue<Double> rawOverallTransferLatencyQueue;
    private static List<BlockingQueue<Double>> rawProcessingLatencyQueueList;
    
    // Static statistical data
    private static final AtomicInteger processedQueryCount = new AtomicInteger(0);
    private static final AtomicDouble energyConsumption = new AtomicDouble(0);
    
    // Private constructor to prevent instantiation
    private MetricsDataProcessor() {
    }
    
    /**
     * Initialize data processor
     */
    public static void initialize() {
        initializeDataQueues();
    }
    
    /**
     * Initialize data queues
     */
    private static void initializeDataQueues() {
        rawOverallProcessingLatencyQueue = new LinkedBlockingQueue<>();
        rawOverallTransferLatencyQueue = new LinkedBlockingQueue<>();
        rawProcessingLatencyQueueList = new ArrayList<>();
        
        int layerCount = DockerRuntimeData.getLayerList().size();
        for (int i = 0; i < layerCount; i++) {
            rawProcessingLatencyQueueList.add(new LinkedBlockingQueue<>());
        }
    }
    
    /**
     * Calculate user-defined metrics
     * 
     * @param metrics the metrics data
     * @param cpuUsageValue the CPU usage value for the node (extracted from DockerRunner)
     * @param configuration the metrics configuration
     */
    public static <T extends BuiltInMetrics> void calculateUserDefinedMetrics(T metrics, double cpuUsageValue, MetricsConfiguration configuration) {
        int nodeId = metrics.getFromNodeId();
        if (nodeId >= 0 && nodeId < configuration.getNodeEnergyParams().size()) {
            double nodeEnergy = configuration.getNodeEnergyParams().get(nodeId);
            energyConsumption.addAndGet(nodeEnergy * cpuUsageValue);
        }
    }
    
    /**
     * Process completed stream
     * 
     * @param metricsList the list of metrics
     * @param configuration the metrics configuration
     */
    public static <T extends BuiltInMetrics> void processCompletedStream(List<T> metricsList, MetricsConfiguration configuration) {
        processedQueryCount.incrementAndGet();
        
        // Calculate overall processing latency
        double overallProcessingLatency = calculateOverallProcessingLatency(metricsList);
        addToQueue(rawOverallProcessingLatencyQueue, overallProcessingLatency);
        logger.info("Added overall processing latency to queue, queue size: {}", rawOverallProcessingLatencyQueue.size());
        
        // Calculate overall transfer latency and energy consumption
        double overallTransferLatency = calculateOverallTransferLatencyAndEnergy(metricsList, configuration);
        addToQueue(rawOverallTransferLatencyQueue, overallTransferLatency);

        // Process layer latencies
        processLayerLatencies(metricsList);
    }
    
    /**
     * Calculate overall processing latency
     */
    private static <T extends BuiltInMetrics> double calculateOverallProcessingLatency(List<T> metricsList) {
        if (metricsList.isEmpty()) return 0.0;
        
        T first = metricsList.get(0);
        T last = metricsList.get(metricsList.size() - 1);
        
        return (double)(last.getTimestampOut() - first.getTimestampIn()) / NANO_TO_MILLI;
    }
    
    /**
     * Calculate overall transfer latency and energy consumption
     */
    private static <T extends BuiltInMetrics> double calculateOverallTransferLatencyAndEnergy(List<T> metricsList, MetricsConfiguration configuration) {
        double overallTransferLatency = 0.0;
        
        for (int i = 0; i < metricsList.size() - 1; i++) {
            T current = metricsList.get(i);
            T next = metricsList.get(i + 1);
            
            // Calculate transfer energy consumption
            calculateTransferEnergy(current, configuration);
            
            // Calculate transfer latency
            overallTransferLatency += (double)(next.getTimestampIn() - current.getTimestampOut()) / NANO_TO_MILLI;
        }
        
        return overallTransferLatency;
    }
    
    /**
     * Calculate transfer energy consumption
     */
    private static <T extends BuiltInMetrics> void calculateTransferEnergy(T metrics, MetricsConfiguration configuration) {
        int fromNodeId = metrics.getFromNodeId();
        int toNodeId = metrics.getToNodeId();
        long packageLength = (long)metrics.getPackageLength();
        
        List<Double> transferParams = configuration.getTransferEnergyParams();
        
        if (fromNodeId >= 0 && fromNodeId < transferParams.size()) {
            energyConsumption.addAndGet(transferParams.get(fromNodeId) * (double)packageLength);
        }
        
        if (toNodeId >= 0 && toNodeId < transferParams.size()) {
            energyConsumption.addAndGet(transferParams.get(toNodeId) * (double)packageLength);
        }
    }
    
    /**
     * Process layer latency data
     */
    private static <T extends BuiltInMetrics> void processLayerLatencies(List<T> metricsList) {
        for (T metrics : metricsList) {
            try {
                int layerId = DockerRuntimeData.getLayerIdByName(
                    DockerRuntimeData.getLayerNameByNodeName(
                        DockerRuntimeData.getNodeNameById(metrics.getFromNodeId())
                    )
                );
                
                if (layerId >= 0 && layerId < rawProcessingLatencyQueueList.size()) {
                    double processingLatency = (double)(metrics.getTimestampOut() - metrics.getTimestampIn()) / NANO_TO_MILLI;
                    addToQueue(rawProcessingLatencyQueueList.get(layerId), processingLatency);
                }
            } catch (Exception e) {
                logger.warn("Error occurred while processing layer latency data: {}", metrics, e);
            }
        }
    }
    
    /**
     * Safely add data to queue
     */
    private static void addToQueue(BlockingQueue<Double> queue, double value) {
        // logger.info("Adding data to queue: {}", value);
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while adding data to queue", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Getter methods for retrieving and resetting statistical data
    public static double getAndResetEnergyConsumption() {
        return energyConsumption.getAndSet(0);
    }
    
    public static int getAndResetProcessedQueryCount() {
        return processedQueryCount.getAndSet(0);
    }
    
    public static Double calculateAverageOverallProcessingLatency() {
        // logger.info("Calculating average overall processing latency, queue:{}", rawOverallProcessingLatencyQueue);
        return calculateAverageFromQueue(rawOverallProcessingLatencyQueue);
    }
    
    public static Double calculateAverageOverallTransferLatency() {
        return calculateAverageFromQueue(rawOverallTransferLatencyQueue);
    }
    
    public static List<Double> calculateLayerProcessingLatencies() {
        List<Double> layerLatencies = new ArrayList<>();
        
        for (BlockingQueue<Double> queue : rawProcessingLatencyQueueList) {
            Double average = calculateAverageFromQueue(queue);
            layerLatencies.add(average != null ? average : 0.0);
        }
        
        return layerLatencies.isEmpty() ? null : layerLatencies;
    }
    
    /**
     * Calculate average from queue
     */
    private static Double calculateAverageFromQueue(BlockingQueue<Double> queue) {
        logger.info("Calculating average from queue: {}", queue);
        if (queue.isEmpty()) return null;
        
        double sum = 0.0;
        int count = 0;
        
        Double value;
        while ((value = queue.poll()) != null) {
            sum += value;
            count++;
        }
        logger.info("Calculated average from queue: {}", sum / count);
        return count > 0 ? sum / count : null;
    }
}