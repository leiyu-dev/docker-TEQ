package org.teq.mearsurer.receiver.sending;

import org.teq.configurator.unserializable.InfoType;
import org.teq.mearsurer.BuiltInMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Stream validator
 * Responsible for validating the validity of metrics data streams
 */
public class StreamValidator {
    
    /**
     * Validate if the stream is valid
     * 
     * @param metricsSet metrics data set
     * @return true if the stream is valid, false otherwise
     */
    public static <T extends BuiltInMetrics> boolean validateStream(Set<T> metricsSet) {
        List<T> metricsList = new ArrayList<>(metricsSet);
        
        if (metricsList.isEmpty()) {
            return false;
        }
        
        // Check if the last metrics is response type and points to sink node
        T lastMetrics = metricsList.get(metricsList.size() - 1);
        if (lastMetrics.getInfoType() != InfoType.Respond || lastMetrics.getToNodeId() != -1) {
            return false;
        }
        
        // Check stream continuity: previous target node should be the source node of the next
        for (int i = 0; i < metricsList.size() - 1; i++) {
            if (metricsList.get(i).getToNodeId() != metricsList.get(i + 1).getFromNodeId()) {
                return false;
            }
        }
        
        return true;
    }
}