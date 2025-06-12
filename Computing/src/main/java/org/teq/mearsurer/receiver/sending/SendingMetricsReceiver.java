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
    
    /** Maximum retry attempts for Flink stream processing */
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
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
     */
    public void requestRestart() {
        logger.info("Restart requested for metrics receiver: {}", typeClass.getSimpleName());
        shouldRestart = true;
        
        // Interrupt current Flink job thread if it exists
        if (flinkJobThread != null && flinkJobThread.isAlive()) {
            flinkJobThread.interrupt();
        }
    }
    
    /**
     * Main running logic with restart capability
     * Initialize components and start Flink stream processing with restart handling
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Reset restart flag
                shouldRestart = false;
                isRunning = true;
                
                // Initialize components
                initializeComponents();
                
                // Start supporting threads
                startSupportingThreads();
                
                // Start Flink stream processing (this will block until job completes or fails)
                startFlinkStreamProcessingWithRestart();
                
            } catch (InterruptedException e) {
                logger.info("Metrics receiver thread interrupted: {}", typeClass.getSimpleName());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error in metrics receiver main loop: {}", typeClass.getSimpleName(), e);
                
                // Wait before retry
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                isRunning = false;
            }
            
            // Check if we should restart
            if (shouldRestart) {
                logger.info("Restarting metrics receiver: {}", typeClass.getSimpleName());
                try {
                    // Wait for port to be completely released
                    logger.info("Waiting for port {} to be released before restart", SimulatorConfig.MetricsReceiverPort);
                    if (!PortUtils.waitForPortAvailable(SimulatorConfig.MetricsReceiverPort, 15000, 1000)) {
                        logger.warn("Port {} may still be in use, proceeding with restart anyway", SimulatorConfig.MetricsReceiverPort);
                    }
                    
                    // Additional wait for Docker containers to be ready
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // If not restarting, break the loop
                logger.info("Metrics receiver stopped: {}", typeClass.getSimpleName());
                break;
            }
        }
    }
    
    // ==================== Private Methods ====================
    
    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // 1. Initialize configuration parameters
        initializeConfiguration();
        
        // 2. Reset and initialize data processor (important for restart scenarios)
        MetricsDataProcessor.reset();
        MetricsDataProcessor.initialize();
        
        // 3. Initialize and register charts
        chartManager.initializeCharts();
        
        logger.info("Components initialization completed for: {}", typeClass.getSimpleName());
    }
    
    /**
     * Start supporting threads (data processing and CPU usage update)
     */
    private void startSupportingThreads() {
        // 4. Start data processing thread
        startDataProcessingThread();
        
        // 5. Start CPU usage update thread
        startCpuUsageUpdateThread();
        
        logger.info("Supporting threads started for: {}", typeClass.getSimpleName());
    }
    
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
        Thread processingThread = new Thread(this::processDataPeriodically, "DataProcessor-" + typeClass.getSimpleName());
        processingThread.setDaemon(true);
        processingThread.start();
        logger.info("Data processing thread started");
    }
    
    /**
     * Start CPU usage update thread
     * Periodically update CPU usage data for the static processor
     */
    private void startCpuUsageUpdateThread() {
        Thread cpuUpdateThread = new Thread(this::updateCpuUsagePeriodically, "CpuUsageUpdater-" + typeClass.getSimpleName());
        cpuUpdateThread.setDaemon(true);
        cpuUpdateThread.start();
        logger.info("CPU usage update thread started");
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
                
                if (isRunning) {
                    // Extract CPU usage from DockerRunner and set it in MetricsProcessor
                    if (dockerRunner.cpuUsageArray != null) {
                        int arraySize = dockerRunner.cpuUsageArray.length();
                        double[] cpuUsage = new double[arraySize];
                        for (int i = 0; i < arraySize; i++) {
                            cpuUsage[i] = dockerRunner.cpuUsageArray.get(i);
                        }
                        MetricsProcessor.setCpuUsageArray(cpuUsage);
                    }
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
     * Start Flink stream processing with restart capability
     */
    private void startFlinkStreamProcessingWithRestart() throws Exception {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRY_ATTEMPTS && !shouldRestart && isRunning) {
            try {
                logger.info("Starting Flink stream processing job (attempt {})", retryCount + 1);
                
                // Create new Flink job thread
                flinkJobThread = new Thread(() -> {
                    try {
                        startFlinkStreamProcessing();
                    } catch (Exception e) {
                        if (!shouldRestart && isRunning) {
                            logger.error("Flink stream processing failed", e);
                        } else {
                            logger.info("Flink stream processing stopped due to restart request");
                        }
                    }
                }, "FlinkJob-" + typeClass.getSimpleName());
                
                flinkJobThread.start();
                flinkJobThread.join(); // Wait for completion
                
                // If we reach here and not restarting, the job completed normally
                if (!shouldRestart) {
                    logger.info("Flink stream processing completed normally");
                    break;
                }
                
            } catch (InterruptedException e) {
                logger.info("Flink stream processing interrupted");
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                logger.error("Error in Flink stream processing (attempt {})", retryCount + 1, e);
                retryCount++;
                
                if (retryCount < MAX_RETRY_ATTEMPTS && !shouldRestart) {
                    // Increase wait time for port conflicts
                    int waitTime = 3000 + (retryCount * 2000); // 3s, 5s, 7s for successive retries
                    logger.info("Retrying Flink stream processing in {} ms...", waitTime);
                    Thread.sleep(waitTime);
                } else {
                    logger.error("Max retry attempts reached or restart requested");
                    break;
                }
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
        logger.info("Executing Flink stream processing job");
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
        // Create new MetricsProcessor instance for each restart
        MetricsProcessor<T> metricsProcessor = new MetricsProcessor<>(dataProcessor);
        stream.map(metricsProcessor)
              .setParallelism(1);
    }
}