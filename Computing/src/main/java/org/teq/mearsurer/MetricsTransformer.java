package org.teq.mearsurer;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teq.mearsurer.receiver.AbstractReceiver;
import org.teq.mearsurer.receiver.DockerMetricsReceiver;
import org.teq.mearsurer.receiver.SendingMetricsReceiver;
import org.teq.simulator.Simulator;
import org.teq.simulator.docker.DockerRunner;
import org.teq.utils.DockerRuntimeData;
import org.teq.utils.connector.CommonDataReceiver;
import org.teq.visualizer.Chart;
import org.teq.visualizer.MetricsDisplayer;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MetricsTransformer {
    private static final Logger logger = LogManager.getLogger(MetricsTransformer.class);
    private Simulator simulator;
    private final MetricsDisplayer metricsDisplayer;
    public MetricsTransformer(Simulator simulator, MetricsDisplayer metricsDisplayer) {
        this.simulator = simulator;
        this.metricsDisplayer = metricsDisplayer;
    }
    public void beginTransform() throws Exception {
        //add a thread to add an element into timeQueue every second:
        AbstractReceiver dockerMonitor = new DockerMetricsReceiver(metricsDisplayer, simulator);
        AbstractReceiver sending = new SendingMetricsReceiver<BuiltInMetrics>(metricsDisplayer,BuiltInMetrics.class);
        sending.beginReceive();
        dockerMonitor.beginReceive();
    }
}
