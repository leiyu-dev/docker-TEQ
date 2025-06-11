package org.teq.mearsurer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.receiver.AbstractReceiver;
import org.teq.mearsurer.receiver.DockerMetricsReceiver;
import org.teq.mearsurer.receiver.sending.SendingMetricsReceiver;
import org.teq.simulator.Simulator;
import org.teq.visualizer.MetricsDisplayer;

public class MetricsTransformer {
    private static final Logger logger = LogManager.getLogger(MetricsTransformer.class);
    private Simulator simulator;
    private final MetricsDisplayer metricsDisplayer;
    public MetricsTransformer(Simulator simulator, MetricsDisplayer metricsDisplayer) {
        this.simulator = simulator;
        this.metricsDisplayer = metricsDisplayer;
    }
    public void beginTransform() throws Exception {
        AbstractReceiver dockerMonitor = new DockerMetricsReceiver(metricsDisplayer, simulator);
        AbstractReceiver sending = new SendingMetricsReceiver<BuiltInMetrics>(metricsDisplayer,BuiltInMetrics.class, simulator.getDockerRunner());
        dockerMonitor.beginReceive();
        sending.beginReceive();
    }
}
 