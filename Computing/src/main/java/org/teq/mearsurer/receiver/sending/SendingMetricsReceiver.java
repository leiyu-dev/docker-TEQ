package org.teq.mearsurer.receiver.sending;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.configurator.SimulatorConfig;
import org.teq.mearsurer.BuiltInMetrics;
import org.teq.mearsurer.receiver.AbstractReceiver;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;
import org.teq.visualizer.MetricsDisplayer;

/**
 * Sending metrics data receiver
 * Responsible for receiving, processing and displaying system performance metrics data,
 * including latency, energy consumption and other indicators
 * 
 * @param <T> Metrics data type that extends BuiltInMetrics
 */
public class SendingMetricsReceiver<T extends BuiltInMetrics> extends AbstractReceiver implements Runnable {
    
    // ==================== Constants ====================
    
    /** Data processing time interval (milliseconds) */
    private static final long DATA_PROCESSING_INTERVAL_MS = 1000;
    
    /** CPU usage update interval (milliseconds) */
    private static final long CPU_USAGE_UPDATE_INTERVAL_MS = 500;
    
    // ==================== Static Variables ====================
    
    private static final Logger logger = LogManager.getLogger(SendingMetricsReceiver.class);
    
    // ==================== Instance Variables ====================
    
    /** Docker runner instance */
    private final DockerRunner dockerRunner;
    
    /** Metrics data type */
    private final Class<T> typeClass;
    
    /** Metrics data processor */
    private final MetricsDataProcessor dataProcessor;
    
    /** Chart manager */
    private final ChartManager chartManager;
    
    /** Configuration parameters */
    private final MetricsConfiguration configuration;
    
    // ==================== Constructor ====================
    
    /**
     * Constructor with default MetricsDataProcessor
     * 
     * @param metricsDisplayer Metrics data displayer
     * @param typeClass Metrics data type
     * @param dockerRunner Docker runner
     */
    public SendingMetricsReceiver(MetricsDisplayer metricsDisplayer, Class<T> typeClass, DockerRunner dockerRunner) {
        super(metricsDisplayer);
        this.typeClass = typeClass;
        this.dockerRunner = dockerRunner;
        this.configuration = new MetricsConfiguration();
        this.dataProcessor = new MetricsDataProcessor(configuration);
        this.chartManager = new ChartManager(metricsDisplayer);
    }
    
    /**
     * Constructor with custom MetricsDataProcessor, configuration and chart manager
     * 
     * @param metricsDisplayer Metrics data displayer
     * @param typeClass Metrics data type
     * @param dockerRunner Docker runner
     * @param dataProcessor Custom metrics data processor
     * @param configuration Metrics configuration
     * @param chartManager Chart manager
     */
    public SendingMetricsReceiver(MetricsDisplayer metricsDisplayer, Class<T> typeClass, 
                                DockerRunner dockerRunner, MetricsDataProcessor dataProcessor, MetricsConfiguration configuration, ChartManager chartManager) {
        super(metricsDisplayer);
        this.typeClass = typeClass;
        this.dockerRunner = dockerRunner;
        this.configuration = configuration;
        this.dataProcessor = dataProcessor;
        this.chartManager = chartManager;
    }
    
    
    // ==================== Public Methods ====================
    
    /**
     * Start receiving metrics data
     * Start data receiving process in a new thread
     */
    @Override
    public void beginReceive() {
        Thread receiverThread = new Thread(this, "MetricsReceiver-" + typeClass.getSimpleName());
        receiverThread.setDaemon(true);
        receiverThread.start();
        logger.info("Metrics data receiver started, data type: {}", typeClass.getSimpleName());
    }
    
    /**
     * Main running logic
     * Initialize components and start Flink stream processing
     */
    @Override
    public void run() {
        try {
            // 1. Initialize configuration parameters
            initializeConfiguration();
            
            // 2. Initialize data processor
            MetricsDataProcessor.initialize();
            
            // 3. Initialize and register charts
            chartManager.initializeCharts();
            
            // 4. Start data processing thread
            startDataProcessingThread();
            
            // 5. Start CPU usage update thread
            startCpuUsageUpdateThread();
            
            // 6. Start Flink stream processing
            startFlinkStreamProcessing();
            
        } catch (Exception e) {
            logger.error("Metrics data receiver failed to run", e);
            throw new RuntimeException("Metrics data receiver failed to run", e);
        }
    }
    
    // ==================== Private Methods ====================
    
    /**
     * Initialize configuration parameters
     */
    private void initializeConfiguration() {
        configuration.initializeEnergyParameters();
        logger.info("Configuration parameters initialization completed");
    }
    
    /**
     * Start data processing thread
     * Periodically process raw data and update charts
     */
    private void startDataProcessingThread() {
        Thread processingThread = new Thread(this::processDataPeriodically, "DataProcessor");
        processingThread.setDaemon(true);
        processingThread.start();
        logger.info("Data processing thread started");
    }
    
    /**
     * Start CPU usage update thread
     * Periodically update CPU usage data for the static processor
     */
    private void startCpuUsageUpdateThread() {
        Thread cpuUpdateThread = new Thread(this::updateCpuUsagePeriodically, "CpuUsageUpdater");
        cpuUpdateThread.setDaemon(true);
        cpuUpdateThread.start();
        logger.info("CPU usage update thread started");
    }
    
    /**
     * Main loop for periodic data processing
     */
    private void processDataPeriodically() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(DATA_PROCESSING_INTERVAL_MS);
                chartManager.processAndUpdateCharts();
            } catch (InterruptedException e) {
                logger.warn("Data processing thread was interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error occurred during data processing", e);
            }
        }
    }
    
    /**
     * Main loop for periodic CPU usage updates
     */
    private void updateCpuUsagePeriodically() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(CPU_USAGE_UPDATE_INTERVAL_MS);
                
                // Extract CPU usage from DockerRunner and set it in MetricsProcessor
                if (dockerRunner.cpuUsageArray != null) {
                    int arraySize = dockerRunner.cpuUsageArray.length();
                    double[] cpuUsage = new double[arraySize];
                    for (int i = 0; i < arraySize; i++) {
                        cpuUsage[i] = dockerRunner.cpuUsageArray.get(i);
                    }
                    MetricsProcessor.setCpuUsageArray(cpuUsage);
                }
            } catch (InterruptedException e) {
                logger.warn("CPU usage update thread was interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error occurred during CPU usage update", e);
            }
        }
    }
    
    /**
     * Start Flink stream processing
     */
    private void startFlinkStreamProcessing() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        
        // Create data stream
        DataStream<T> stream = createDataStream(env);
        
        // Process data stream
        processDataStream(stream);
        
        // Execute Flink job
        logger.info("Starting Flink stream processing job");
        env.execute("MetricsReceiver-" + typeClass.getSimpleName());
    }
    
    /**
     * Create data stream
     */
    private DataStream<T> createDataStream(StreamExecutionEnvironment env) {
        return env.addSource(new CommonDataReceiver<>(SimulatorConfig.MetricsReceiverPort, typeClass))
                 .returns(TypeInformation.of(typeClass));
    }
    
    /**
     * Process data stream
     */
    private void processDataStream(DataStream<T> stream) {
        stream.map(new MetricsProcessor<>(dataProcessor))
              .setParallelism(1);
    }
}