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
import org.teq.utils.PortUtils;
import org.teq.utils.connector.flink.javasocket.CommonDataReceiver;
import org.teq.visualizer.MetricsDisplayer;

/**
 * Sending metrics data receiver
 * Responsible for receiving, processing and displaying system performance metrics data,
 * including latency, energy consumption and other indicators
 * 
 * Features:
 * - Continuous Flink stream processing with auto-reconnection
 * - Lightweight restart mechanism that only reinitializes data components
 * - Separate monitoring thread for handling restart requests
 * 
 * @param <T> Metrics data type that extends BuiltInMetrics
 */
public class SendingMetricsReceiver<T extends BuiltInMetrics> extends AbstractReceiver implements Runnable {
    
    // ==================== Constants ====================
    
    /** Data processing time interval (milliseconds) */
    private static final long DATA_PROCESSING_INTERVAL_MS = 1000;
    
    /** CPU usage update interval (milliseconds) */
    private static final long CPU_USAGE_UPDATE_INTERVAL_MS = 500;
    
    /** Simulator state check interval (milliseconds) */
    private static final long SIMULATOR_STATE_CHECK_INTERVAL_MS = 1000;
    
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
    
    /** Control flags for restart mechanism */
    private volatile boolean shouldRestart = false;
    private volatile boolean isRunning = false;
    private volatile Thread flinkJobThread = null;
    
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
     * Request restart of the receiver
     * This method can be called when Simulator restart is detected
     * Only reinitializes data processing components without stopping Flink job
     */
    public void requestRestart() {
        logger.info("Restart requested for metrics receiver: {}", typeClass.getSimpleName());
        shouldRestart = true;
        
        // No longer interrupt Flink job thread as CommonDataReceiver has auto-reconnection
        logger.info("Data components will be reinitialized while keeping Flink job running");
    }
    
    /**
     * Main running logic with restart capability
     * Initialize components and start Flink stream processing with restart handling
     */
    @Override
    public void run() {
        try {
            shouldRestart = false;
            isRunning = true;
            
            // Initialize all components
            initializeAllComponents();
            
            // Start Flink stream processing
            startFlinkStreamProcessing();
            
            // Start restart monitoring thread
            startRestartMonitoringThread();
            
            // Keep main thread alive to handle restarts
            while (!Thread.currentThread().isInterrupted() && isRunning) {
                try {
                    Thread.sleep(SIMULATOR_STATE_CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
        } catch (InterruptedException e) {
            logger.info("Metrics receiver thread interrupted: {}", typeClass.getSimpleName());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error in metrics receiver main loop: {}", typeClass.getSimpleName(), e);
        } finally {
            isRunning = false;
        }
    }
    
    // ==================== Private Methods ====================
    
    /**
     * Initialize all components including configuration, data processor, charts and supporting threads
     */
    private void initializeAllComponents() {
        // Initialize configuration parameters
        configuration.initializeEnergyParameters();
        logger.info("Configuration parameters initialization completed");
        
        // Reset and initialize data processor
        MetricsDataProcessor.reset();
        MetricsDataProcessor.initialize();
        
        // Initialize and register charts
        chartManager.initializeCharts();
        
        // Start data processing thread
        Thread processingThread = new Thread(this::processDataPeriodically, "DataProcessor-" + typeClass.getSimpleName());
        processingThread.setDaemon(true);
        processingThread.start();
        logger.info("Data processing thread started");
        
        // Start CPU usage update thread
        Thread cpuUpdateThread = new Thread(this::updateCpuUsagePeriodically, "CpuUsageUpdater-" + typeClass.getSimpleName());
        cpuUpdateThread.setDaemon(true);
        cpuUpdateThread.start();
        logger.info("CPU usage update thread started");
        
        logger.info("All components initialization completed for: {}", typeClass.getSimpleName());
    }
    
    /**
     * Main loop for periodic data processing
     */
    private void processDataPeriodically() {
        while (!Thread.currentThread().isInterrupted() && isRunning) {
            try {
                Thread.sleep(DATA_PROCESSING_INTERVAL_MS);
                if (isRunning) {
                    chartManager.processAndUpdateCharts();
                }
            } catch (InterruptedException e) {
                logger.warn("Data processing thread was interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error occurred during data processing", e);
            }
        }
        logger.info("Data processing thread stopped");
    }
    
    /**
     * Main loop for periodic CPU usage updates
     */
    private void updateCpuUsagePeriodically() {
        while (!Thread.currentThread().isInterrupted() && isRunning) {
            try {
                Thread.sleep(CPU_USAGE_UPDATE_INTERVAL_MS);
                
                if (isRunning && dockerRunner.cpuUsageArray != null) {
                    // Extract CPU usage from DockerRunner and set it in MetricsProcessor
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
        logger.info("CPU usage update thread stopped");
    }
    
    /**
     * Start restart monitoring thread
     * Monitors for restart requests and reinitializes data components
     */
    private void startRestartMonitoringThread() {
        Thread restartMonitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && isRunning) {
                try {
                    Thread.sleep(SIMULATOR_STATE_CHECK_INTERVAL_MS);
                    
                    if (shouldRestart && isRunning) {
                        logger.info("Processing restart request for metrics receiver: {}", typeClass.getSimpleName());
                        
                        // Reinitialize data components without stopping Flink
                        logger.info("Reinitializing data components...");
                        
                        // Reset and reinitialize data processor
                        MetricsDataProcessor.reset();
                        MetricsDataProcessor.initialize();
                        
                        // Reinitialize configuration
                        configuration.initializeEnergyParameters();
                        
                        // do not need to reinitialize charts
                        
                        // Reset restart flag
                        shouldRestart = false;
                        
                        logger.info("Data components reinitialized successfully");
                    }
                } catch (InterruptedException e) {
                    logger.info("Restart monitoring thread interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in restart monitoring thread", e);
                }
            }
        }, "RestartMonitor-" + typeClass.getSimpleName());
        
        restartMonitorThread.setDaemon(true);
        restartMonitorThread.start();
        logger.info("Restart monitoring thread started");
    }
    
    /**
     * Start Flink stream processing in a separate thread
     * Flink job will run continuously with auto-reconnection capability
     */
    private void startFlinkStreamProcessing() throws Exception {
        // Create and start Flink job thread
        flinkJobThread = new Thread(() -> {
            try {
                logger.info("Starting Flink stream processing job");
                
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
                env.setParallelism(1);
                
                // Create and process data stream
                DataStream<T> stream = env.addSource(new CommonDataReceiver<>(SimulatorConfig.MetricsReceiverPort, typeClass))
                                         .returns(TypeInformation.of(typeClass));
                
                MetricsProcessor<T> metricsProcessor = new MetricsProcessor<>(dataProcessor);
                stream.map(metricsProcessor).setParallelism(1);
                
                // Execute Flink job (this will block until job completes)
                logger.info("Executing Flink stream processing job with auto-reconnection");
                env.execute("MetricsReceiver-" + typeClass.getSimpleName());
                
            } catch (Exception e) {
                if (isRunning) {
                    logger.error("Flink stream processing failed", e);
                } else {
                    logger.info("Flink stream processing stopped");
                }
            } finally {
                isRunning = false;
            }
        }, "FlinkJob-" + typeClass.getSimpleName());
        
        flinkJobThread.setDaemon(true);
        flinkJobThread.start();
        
        // Wait a moment to ensure Flink job starts
        Thread.sleep(1000);
        logger.info("Flink stream processing thread started");
    }
}