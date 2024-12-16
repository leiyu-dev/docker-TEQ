package org.teq.visualizer;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Chart<X,Y>{
    @JSONField(serialize = false)
    private BlockingQueue<X>xAxis;

    @JSONField(serialize = false)
    private List<BlockingQueue<Y>> yAxis;
    @JSONField(serialize = true)
    private String xLabel;
    @JSONField(serialize = true)
    private List<String> dataLabel;
    @JSONField(serialize = true)
    private String yLabel;
    @JSONField(serialize = true)
    private String title;

    public Chart(BlockingQueue<X> xAxis, List<BlockingQueue<Y>> yAxis, String xLabel,String yLabel, List<String> dataLabel, String title){
        this.setxAxis(xAxis);
        this.setyAxis(yAxis);
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(dataLabel);
        this.setTitle(title);
    }
    //only one yData
    public Chart(BlockingQueue<X> xAxis, BlockingQueue<Y> yAxis, String xLabel,String yLabel, String dataLabel, String title){
        this.setxAxis(xAxis);
        this.setyAxis(List.of(yAxis));
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(List.of(dataLabel));
        this.setTitle(title);
    }

    /**
     * it is the user's duty to ensure that two BlockingQueues have the same size at any time,
     * otherwise the program will crash
     */
    @JSONField(serialize = false)
    public BlockingQueue<?> getxAxis() {
        return xAxis;
    }

    public void setxAxis(BlockingQueue<X> xAxis) {
        this.xAxis = xAxis;
    }

    @JSONField(serialize = false)
    public List<BlockingQueue<Y>> getyAxis() {
        return yAxis;
    }

    public void setyAxis(List<BlockingQueue<Y>> yAxis) {
        this.yAxis = yAxis;
    }

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public List<String> getDataLabel() {
        return dataLabel;
    }

    public void setDataLabel(List<String> dataLabel) {
        this.dataLabel = dataLabel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getyLabel() {
        return yLabel;
    }

    public void setyLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    //json format
    public String toString(){
        return "{\n" +
                "  \"xLabel\": \"" + getxLabel() + "\",\n" +
                "  \"yLabel\": \"" + getyLabel() + "\",\n" +
                "  \"dataLabel\": \"" + getDataLabel() + "\",\n" +
                "  \"title\": \"" + getTitle() + "\"\n" +
                "}";
    }


}
