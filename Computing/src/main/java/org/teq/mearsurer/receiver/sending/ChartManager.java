package org.teq.mearsurer.receiver.sending;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.utils.DockerRuntimeData;
import org.teq.visualizer.MetricsDisplayer;
import org.teq.visualizer.TimeChart;

import java.util.List;

/**
 * Chart manager
 * Responsible for managing and updating various charts
 */
public class ChartManager {
    
    private static final Logger logger = LogManager.getLogger(ChartManager.class);
    
    private final MetricsDisplayer metricsDisplayer;
    
    // Chart instances
    private TimeChart<Double> overallProcessingLatencyChart;
    private TimeChart<Double> overallTransferLatencyChart;
    private TimeChart<Double> processingLatencyChart;
    private TimeChart<Integer> processedQueryChart;
    private TimeChart<Double> energyConsumptionChart;
    
    public ChartManager(MetricsDisplayer metricsDisplayer) {
        this.metricsDisplayer = metricsDisplayer;
    }
    
    /**
     * Initialize all charts
     */
    public void initializeCharts() {
        createCharts();
        registerCharts();
        logger.info("Chart initialization completed");
    }
    
    /**
     * Create chart instances
     */
    private void createCharts() {
        overallProcessingLatencyChart = new TimeChart<>(
            "overall processing latency/ms", "latency", "overall processing latency", "overview"
        );
        
        overallTransferLatencyChart = new TimeChart<>(
            "overall transfer latency/ms", "latency", "overall transfer latency", "overview"
        );
        
        processingLatencyChart = new TimeChart<>(
            "processing latency/ms", DockerRuntimeData.getLayerList(), "processing latency", "overview"
        );
        
        processedQueryChart = new TimeChart<>(
            "processed query", "count", "processed query", "user"
        );
        
        energyConsumptionChart = new TimeChart<>(
            "energy consumption", "Energy Unit", "energy consumption", "overview"
        );
    }
    
    /**
     * Register charts to displayer
     */
    private void registerCharts() {
        metricsDisplayer.addChart(overallProcessingLatencyChart);
        metricsDisplayer.addChart(overallTransferLatencyChart);
        metricsDisplayer.addChart(processingLatencyChart);
        metricsDisplayer.addChart(processedQueryChart);
        metricsDisplayer.addChart(energyConsumptionChart);
    }
    
    /**
     * Process data and update all charts
     */
    public void processAndUpdateCharts() {
        
        // Update energy consumption chart
        updateEnergyConsumptionChart(MetricsDataProcessor.getAndResetEnergyConsumption());
        
        // Update processed query count chart
        updateProcessedQueryChart(MetricsDataProcessor.getAndResetProcessedQueryCount());
        
        // Update overall processing latency chart
        updateOverallProcessingLatencyChart(
            MetricsDataProcessor.calculateAverageOverallProcessingLatency()
        );
        
        // Update overall transfer latency chart
        updateOverallTransferLatencyChart(
            MetricsDataProcessor.calculateAverageOverallTransferLatency()
        );
        
        // Update layer processing latency chart
        updateProcessingLatencyChart(
            MetricsDataProcessor.calculateLayerProcessingLatencies()
        );
    }
    
    // Chart update methods
    public void updateEnergyConsumptionChart(double value) {
        energyConsumptionChart.addDataPoint(value);
    }
    
    public void updateProcessedQueryChart(int count) {
        processedQueryChart.addDataPoint(count);
    }
    
    public void updateOverallProcessingLatencyChart(Double averageLatency) {
        if (averageLatency != null) {
            overallProcessingLatencyChart.addDataPoint(averageLatency);
        }
    }
    
    public void updateOverallTransferLatencyChart(Double averageLatency) {
        if (averageLatency != null) {
            overallTransferLatencyChart.addDataPoint(averageLatency);
        }
    }
    
    public void updateProcessingLatencyChart(List<Double> layerLatencies) {
        if (layerLatencies != null && !layerLatencies.isEmpty()) {
            processingLatencyChart.addDataPoint(layerLatencies);
        }
    }
}