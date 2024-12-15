package org.teq.mearsurer.receiver;

import org.teq.visualizer.MetricsDisplayer;

public abstract class AbstractReceiver{
    MetricsDisplayer metricsDisplayer;
    public AbstractReceiver(MetricsDisplayer metricsDisplayer){
        this.metricsDisplayer = metricsDisplayer;
    }
    public abstract void beginReceive();
}
