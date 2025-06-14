package org.teq.visualizer;

import java.util.ArrayList;
import java.util.List;

public abstract class MetricsDisplayer {
    List<Chart> chartList = new ArrayList<>();
    public abstract void addChart(Chart chart);
    public abstract void display();
}
