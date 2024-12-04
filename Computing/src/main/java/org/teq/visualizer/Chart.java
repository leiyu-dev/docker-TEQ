package org.teq.visualizer;

import java.util.concurrent.BlockingQueue;

public class Chart{

    private BlockingQueue<Object>xAxis;
    private BlockingQueue<Object>yAxis;
    private String xLabel;
    private String yLabel;
    private String title;

    public Chart(BlockingQueue xAxis, BlockingQueue yAxis, String xLabel, String yLabel, String title){
        this.setxAxis((BlockingQueue<Object>) xAxis);
        this.setyAxis((BlockingQueue<Object>) yAxis);
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setTitle(title);
    }

    /**
     * it is the user's duty to ensure that two BlockingQueues have the same size at any time,
     * otherwise the program will crash
     */
    public BlockingQueue<Object> getxAxis() {
        return xAxis;
    }

    public void setxAxis(BlockingQueue<Object> xAxis) {
        this.xAxis = xAxis;
    }

    public BlockingQueue<Object> getyAxis() {
        return yAxis;
    }

    public void setyAxis(BlockingQueue<Object> yAxis) {
        this.yAxis = yAxis;
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
