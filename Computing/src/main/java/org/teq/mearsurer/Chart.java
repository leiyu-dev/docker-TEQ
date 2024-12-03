package org.teq.mearsurer;

import java.util.concurrent.BlockingQueue;

public class Chart{

    /**
     * it is the user's duty to ensure that two BlockingQueues have the same size at any time,
     * otherwise the program will crash
     */
    BlockingQueue<Object>xAxis;
    BlockingQueue<Object> yAxis;
    String xLabel;
    String yLabel;
    String title;

    Chart(BlockingQueue<Object> xAxis, BlockingQueue<Object> yAxis, String xLabel, String yLabel, String title){
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.title = title;
    }
}
