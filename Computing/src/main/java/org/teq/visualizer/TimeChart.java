package org.teq.visualizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teq.utils.NullPlaceholder;
import org.teq.utils.utils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TimeChart is a subclass of Chart, specifically designed for time series charts
 * The x-axis automatically manages time data, users only need to add data to the y-axis
 */
public class TimeChart<Y> extends Chart<Double, Y> {
    private static final Logger logger = LoggerFactory.getLogger(TimeChart.class);
    private AtomicLong startTime = null;
    
    /**
     * Constructor for multiple Y-axis data series
     */
    public TimeChart(String yLabel, List<String> dataLabel, String title, String type) {
        super(new LinkedBlockingQueue<>(), createYAxisQueues(dataLabel.size()), 
              "time/s", yLabel, dataLabel, title, type);
        this.startTime = utils.getStartTime();
    }
    
    /**
     * Constructor for single Y-axis data series
     */
    public TimeChart(String yLabel, String dataLabel, String title, String type) {
        super(new LinkedBlockingQueue<>(), new LinkedBlockingQueue<>(), 
              "time/s", yLabel, dataLabel, title, type);
        this.startTime = utils.getStartTime();
    }
    
    /**
     * Create specified number of Y-axis queues
     */
    private static <Y> List<BlockingQueue<Y>> createYAxisQueues(int count) {
        List<BlockingQueue<Y>> queues = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            queues.add(new LinkedBlockingQueue<>());
        }
        return queues;
    }
    
    /**
     * Get X-axis queue (time queue)
     */
    @SuppressWarnings("unchecked")
    private BlockingQueue<Double> getTimeQueue() {
        return (BlockingQueue<Double>) getxAxis();
    }
    
    /**
     * Add data point (multiple Y-axis data)
     * Automatically adds current time to x-axis, adds Y-axis data to corresponding queues
     */
    public void addDataPoint(List<Y> yValues) {
        if (yValues.size() != getyAxis().size()) {
            throw new IllegalArgumentException("Number of Y-axis data does not match number of queues");
        }
        
        try {
            // Add current time
            long currentTime = System.currentTimeMillis();
            double timeInSeconds = Math.round((currentTime - startTime.get()) / 1000.0 * 10.0) / 10.0;
            getTimeQueue().put(timeInSeconds);
            
            // Add Y-axis data
            for (int i = 0; i < yValues.size(); i++) {
                if(yValues.get(i) == null){
                    @SuppressWarnings("unchecked")
                    Y placeholder = (Y) new NullPlaceholder();
                    getyAxis().get(i).put(placeholder);
                }
                else{
                    getyAxis().get(i).put(yValues.get(i));
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
    public void addDataPoint(Y yValue) {
        addDataPoint(List.of(yValue));
    }
    
    /**
     * Get Y-axis queues for external direct access
     */
    public List<BlockingQueue<Y>> getYAxisQueues() {
        return getyAxis();
    }
    
    /**
     * Get Y-axis queue at specified index
     */
    public BlockingQueue<Y> getYAxisQueue(int index) {
        return getyAxis().get(index);
    }
    
    /**
     * Reset start time
     */
    public void resetStartTime() {
        this.startTime = new AtomicLong(System.currentTimeMillis());
    }
    
    /**
     * Set start time
     */
    public void setStartTime(long startTime) {
        this.startTime = new AtomicLong(startTime);
    }
    
    /**
     * Get start time
     */
    public AtomicLong getStartTime() {
        return startTime;
    }
} 