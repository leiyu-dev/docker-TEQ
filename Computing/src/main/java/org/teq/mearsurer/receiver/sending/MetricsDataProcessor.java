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
 * Metrics data processor base class
 * Responsible for processing and calculating various metrics
 * Can be extended to customize processing behavior
 */
public class MetricsDataProcessor implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Time unit conversion: nanoseconds to milliseconds */
    protected static final double NANO_TO_MILLI = 1000.0 * 1000.0;
    
    protected static final Logger logger = LogManager.getLogger(MetricsDataProcessor.class);
    
    // Static data queues (shared across all instances)
    private static BlockingQueue<Double> rawOverallProcessingLatencyQueue;
    private static BlockingQueue<Double> rawOverallTransferLatencyQueue;
    private static List<BlockingQueue<Double>> rawProcessingLatencyQueueList;
    
    // Static statistical data (shared across all instances)
    private static final AtomicInteger processedQueryCount = new AtomicInteger(0);
    private static final AtomicDouble energyConsumption = new AtomicDouble(0);
    
    // Instance configuration
    protected final MetricsConfiguration configuration;
    
    /**
     * Constructor
     * 
     * @param configuration the metrics configuration
     */
    public MetricsDataProcessor(MetricsConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Initialize data processor (static initialization)
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
     * Can be overridden by subclasses to customize energy consumption calculation
     * 
     * @param metrics the metrics data
     * @param cpuUsageValue the CPU usage value for the node (extracted from DockerRunner)
     */
    public <T extends BuiltInMetrics> void calculateUserDefinedMetrics(T metrics, double cpuUsageValue) {
        int nodeId = metrics.getFromNodeId();
        if (nodeId >= 0 && nodeId < configuration.getNodeEnergyParams().size()) {
            double nodeEnergy = configuration.getNodeEnergyParams().get(nodeId);
            energyConsumption.addAndGet(nodeEnergy * cpuUsageValue);
        }
    }
    
    /**
     * Process completed stream
     * Can be overridden by subclasses to customize stream processing behavior
     * 
     * @param metricsList the list of metrics
     */
    public <T extends BuiltInMetrics> void processCompletedStream(List<T> metricsList) {
        processedQueryCount.incrementAndGet();
        
        // Calculate overall processing latency
        double overallProcessingLatency = calculateOverallProcessingLatency(metricsList);
        addToQueue(rawOverallProcessingLatencyQueue, overallProcessingLatency);
        logger.info("Added overall processing latency to queue, queue size: {}", rawOverallProcessingLatencyQueue.size());
        
        // Calculate overall transfer latency and energy consumption
        double overallTransferLatency = calculateOverallTransferLatencyAndEnergy(metricsList);
        addToQueue(rawOverallTransferLatencyQueue, overallTransferLatency);

        // Process layer latencies
        processLayerLatencies(metricsList);
    }
    
    /**
     * Calculate overall processing latency
     * Can be overridden by subclasses to customize latency calculation
     */
    protected <T extends BuiltInMetrics> double calculateOverallProcessingLatency(List<T> metricsList) {
        if (metricsList.isEmpty()) return 0.0;
        
        T first = metricsList.get(0);
        T last = metricsList.get(metricsList.size() - 1);
        
        return (double)(last.getTimestampOut() - first.getTimestampIn()) / NANO_TO_MILLI;
    }
    
    /**
     * Calculate overall transfer latency and energy consumption
     * Can be overridden by subclasses to customize transfer processing
     */
    protected <T extends BuiltInMetrics> double calculateOverallTransferLatencyAndEnergy(List<T> metricsList) {
        double overallTransferLatency = 0.0;
        
        for (int i = 0; i < metricsList.size() - 1; i++) {
            T current = metricsList.get(i);
            T next = metricsList.get(i + 1);
            
            // Calculate transfer energy consumption
            calculateTransferEnergy(current);
            
            // Calculate transfer latency
            overallTransferLatency += (double)(next.getTimestampIn() - current.getTimestampOut()) / NANO_TO_MILLI;
        }
        
        return overallTransferLatency;
    }
    
    /**
     * Calculate transfer energy consumption
     * Can be overridden by subclasses to customize energy calculation
     */
    protected <T extends BuiltInMetrics> void calculateTransferEnergy(T metrics) {
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
     * Can be overridden by subclasses to customize layer processing
     */
    protected <T extends BuiltInMetrics> void processLayerLatencies(List<T> metricsList) {
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
    protected static void addToQueue(BlockingQueue<Double> queue, double value) {
        // logger.info("Adding data to queue: {}", value);
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while adding data to queue", e);
            Thread.currentThread().interrupt();
        }
    }
    
    // Static getter methods for retrieving and resetting statistical data
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