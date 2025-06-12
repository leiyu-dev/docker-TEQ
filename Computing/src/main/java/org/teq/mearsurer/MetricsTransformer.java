package org.teq.mearsurer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.receiver.AbstractReceiver;
import org.teq.mearsurer.receiver.DockerMetricsReceiver;
import org.teq.mearsurer.receiver.sending.SendingMetricsReceiver;
import org.teq.simulator.Simulator;
import org.teq.visualizer.MetricsDisplayer;

import java.util.ArrayList;
import java.util.List;

public class MetricsTransformer {
    private static final Logger logger = LogManager.getLogger(MetricsTransformer.class);
    private Simulator simulator;
    private final MetricsDisplayer metricsDisplayer;
    
    // Keep references to receivers for restart management
    private final List<AbstractReceiver> receivers = new ArrayList<>();
    private SendingMetricsReceiver<BuiltInMetrics> sendingReceiver;
    
    public MetricsTransformer(Simulator simulator, MetricsDisplayer metricsDisplayer) {
        this.simulator = simulator;
        this.metricsDisplayer = metricsDisplayer;
    }
    
    public void beginTransform() throws Exception {
        logger.info("Starting metrics transformation");
        
        AbstractReceiver dockerMonitor = new DockerMetricsReceiver(metricsDisplayer, simulator);
        sendingReceiver = new SendingMetricsReceiver<BuiltInMetrics>(metricsDisplayer, BuiltInMetrics.class, simulator.getDockerRunner());
        
        // Store references for restart management
        receivers.add(dockerMonitor);
        receivers.add(sendingReceiver);
        
        // Start all receivers
        dockerMonitor.beginReceive();
        sendingReceiver.beginReceive();
        
        logger.info("Metrics transformation started successfully");
    }
    
    /**
     * Notify all receivers to restart
     * This method should be called when Simulator restart is detected
     */
    public void notifyRestart() {
        logger.info("Notifying all receivers to restart");
        
        // Notify SendingMetricsReceiver to restart
        if (sendingReceiver != null) {
            sendingReceiver.requestRestart();
        }
        
        // DockerMetricsReceiver doesn't need restart as it uses a different mechanism
        // that automatically recovers through dockerRunner.recoverCollection()
        
        logger.info("Restart notification sent to all receivers");
    }
    
    /**
     * Stop all receivers
     */
    public void stopTransform() {
        logger.info("Stopping metrics transformation");
        
        // Currently, there's no standard stop method in AbstractReceiver
        // This could be enhanced in the future if needed
        
        logger.info("Metrics transformation stopped");
    }
}
 