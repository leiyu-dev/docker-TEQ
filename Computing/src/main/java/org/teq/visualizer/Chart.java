package org.teq.visualizer;

import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teq.utils.NullPlaceholder;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Chart<X,Y>{
    private static final Logger logger = LoggerFactory.getLogger(Chart.class);
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
    @JSONField(serialize = true)

    private String type;

    public Chart(BlockingQueue<X> xAxis, List<BlockingQueue<Y>> yAxis, String xLabel,String yLabel, List<String> dataLabel, String title, String type){
        this.setxAxis(xAxis);
        this.setyAxis(yAxis);
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(dataLabel);
        this.setTitle(title);
        this.setType(type);
    }
    //only one yData
    public Chart(BlockingQueue<X> xAxis, BlockingQueue<Y> yAxis, String xLabel,String yLabel, String dataLabel, String title, String type){
        this.setxAxis(xAxis);
        this.setyAxis(List.of(yAxis));
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(List.of(dataLabel));
        this.setTitle(title);
        this.setType(type);
    }
    
    /**
     * Constructor that creates BlockingQueues internally (multiple Y-axis data series)
     */
    public Chart(String xLabel, String yLabel, List<String> dataLabel, String title, String type){
        this.setxAxis(new java.util.concurrent.LinkedBlockingQueue<>());
        
        List<BlockingQueue<Y>> yAxisQueues = new java.util.ArrayList<>();
        for (int i = 0; i < dataLabel.size(); i++) {
            yAxisQueues.add(new java.util.concurrent.LinkedBlockingQueue<>());
        }
        this.setyAxis(yAxisQueues);
        
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(dataLabel);
        this.setTitle(title);
        this.setType(type);
    }
    
    /**
     * Constructor that creates BlockingQueues internally (single Y-axis data series)
     */
    public Chart(String xLabel, String yLabel, String dataLabel, String title, String type){
        this.setxAxis(new java.util.concurrent.LinkedBlockingQueue<>());
        this.setyAxis(List.of(new java.util.concurrent.LinkedBlockingQueue<>()));
        this.setxLabel(xLabel);
        this.setyLabel(yLabel);
        this.setDataLabel(List.of(dataLabel));
        this.setTitle(title);
        this.setType(type);
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

    /**
     * Add data point (multiple Y-axis data)
     * Adds X-axis data and corresponding Y-axis data to their respective queues
     */
    public void addDataPoint(X xValue, List<Y> yValues) {
        if (yValues.size() != yAxis.size()) {
            throw new IllegalArgumentException("Number of Y-axis data does not match number of queues");
        }
        
        try {
            // Add X-axis data
            xAxis.put(xValue);
            
            // Add Y-axis data
            for (int i = 0; i < yValues.size(); i++) {
                if(yValues.get(i) == null){
                    @SuppressWarnings("unchecked")
                    Y placeholder = (Y) new NullPlaceholder();
                    yAxis.get(i).put(placeholder);
                }
                else{
                    yAxis.get(i).put(yValues.get(i));
                }
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while adding data point", e);
            throw new RuntimeException("Interrupted while adding data point", e);
        }
    }
    
    /**
     * Add data point (single Y-axis data)
     */
    public void addDataPoint(X xValue, Y yValue) {
        addDataPoint(xValue, List.of(yValue));
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
