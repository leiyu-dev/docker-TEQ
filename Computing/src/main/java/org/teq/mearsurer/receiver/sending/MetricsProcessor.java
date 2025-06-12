package org.teq.mearsurer.receiver.sending;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.BuiltInMetrics;

import java.io.Serializable;
import java.util.*;

/**
 * Metrics data processor
 * Responsible for processing each metrics data item in the stream
 */
public class MetricsProcessor<T extends BuiltInMetrics> implements MapFunction<T, T>, Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(MetricsProcessor.class);
    
    /** Store metrics data grouped by ID */
    private final Map<UUID, TreeSet<T>> metricsMap = new HashMap<>();
    
    private final MetricsDataProcessor dataProcessor;
    
    // Static reference to CPU usage data - this will be set by the main receiver thread
    private static volatile double[] cpuUsageArray = new double[0];
    
    public MetricsProcessor(MetricsDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
    
    /**
     * Set CPU usage data from external source (called from main thread)
     */
    public static void setCpuUsageArray(double[] cpuUsage) {
        cpuUsageArray = Arrays.copyOf(cpuUsage, cpuUsage.length);
    }
    
    /**
     * Reset processor state for restart
     * Clear internal metrics map
     */
    public void resetProcessor() {
        logger.info("Resetting metrics processor state");
        metricsMap.clear();
        logger.info("Metrics processor state reset completed");
    }
    
    @Override
    public T map(T metrics) throws Exception {
        try {
            // Get CPU usage for the node (with bounds checking)
            double cpuUsageValue = 0.0;
            int nodeId = metrics.getFromNodeId();
            if (nodeId >= 0 && nodeId < cpuUsageArray.length) {
                cpuUsageValue = cpuUsageArray[nodeId];
            }
            
            // Calculate user-defined metrics with CPU usage value
            dataProcessor.calculateUserDefinedMetrics(metrics, cpuUsageValue);
            
            // Process metrics stream
            processMetricsStream(metrics);
            
            return metrics;
        } catch (Exception e) {
            logger.error("Error occurred while processing metrics data: {}", metrics, e);
            throw e;
        }
    }
    
    /**
     * Process metrics data stream
     */
    private void processMetricsStream(T metrics) {
        UUID streamId = metrics.getId();
        
        // Get or create metrics collection
        TreeSet<T> metricsSet = metricsMap.computeIfAbsent(streamId, 
            k -> new TreeSet<>(Comparator.comparingLong(T::getTimestampIn)));
        
        // Add current metrics data
        metricsSet.add(metrics);
        
        // Check if stream is completed (reached sink node)
        if (isStreamCompleted(metricsSet)) {
            processCompletedStream(metricsSet);
            metricsMap.remove(streamId);
        }
    }
    
    /**
     * Check if stream is completed
     */
    private boolean isStreamCompleted(TreeSet<T> metricsSet) {
        T lastMetrics = metricsSet.last();
        return lastMetrics.getToNodeId() == -1; // -1 indicates sink node
    }
    
    /**
     * Process completed stream
     */
    private void processCompletedStream(TreeSet<T> metricsSet) {
        if (StreamValidator.validateStream(metricsSet)) {
            dataProcessor.processCompletedStream(new ArrayList<>(metricsSet));
        }
    }
}