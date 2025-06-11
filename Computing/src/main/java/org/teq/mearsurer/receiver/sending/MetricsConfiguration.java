package org.teq.mearsurer.receiver.sending;

import org.teq.utils.DockerRuntimeData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Metrics configuration class
 * Manages system configuration parameters
 */
public class MetricsConfiguration implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Default node energy parameter */
    private static final double DEFAULT_NODE_ENERGY_PARAM = 1.0;
    
    /** Default transfer energy parameter */
    private static final double DEFAULT_TRANSFER_ENERGY_PARAM = 0.01;
    
    /** Node energy parameter list */
    private List<Double> nodeEnergyParams;
    
    /** Transfer energy parameter list */
    private List<Double> transferEnergyParams;
    
    /**
     * Initialize energy parameters
     */
    public void initializeEnergyParameters() {
        int nodeCount = DockerRuntimeData.getNodeNameList().size();
        
        nodeEnergyParams = new ArrayList<>(nodeCount);
        transferEnergyParams = new ArrayList<>(nodeCount);
        
        for (int i = 0; i < nodeCount; i++) {
            nodeEnergyParams.add(DEFAULT_NODE_ENERGY_PARAM);
            transferEnergyParams.add(DEFAULT_TRANSFER_ENERGY_PARAM);
        }
    }
    
    // Getters
    public List<Double> getNodeEnergyParams() { 
        return nodeEnergyParams; 
    }
    
    public List<Double> getTransferEnergyParams() { 
        return transferEnergyParams; 
    }
}
