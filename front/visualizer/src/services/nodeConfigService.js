import axios from 'axios';

const BASE_URL = 'http://localhost:8889';

/**
 * Node Configuration Service
 * Handles all API calls related to node configuration
 */
export class NodeConfigService {
  /**
   * Fetch available algorithms
   */
  static async getAlgorithms() {
    try {
      const response = await axios.get(`${BASE_URL}/algorithm`);
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch algorithms: ${error.message}`);
    }
  }

  /**
   * Fetch layers for a specific algorithm
   */
  static async getLayers(algorithm = 'Algorithm1') {
    try {
      const response = await axios.get(`${BASE_URL}/layer`, {
        params: { algorithm }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch layers: ${error.message}`);
    }
  }

  /**
   * Fetch nodes for a specific algorithm and layer
   */
  static async getNodes(algorithm = 'Algorithm1', layer) {
    try {
      const response = await axios.get(`${BASE_URL}/node`, {
        params: { algorithm, layer }
      });
      const nodes = response.data;
      nodes.push('All nodes');
      return nodes;
    } catch (error) {
      throw new Error(`Failed to fetch nodes: ${error.message}`);
    }
  }

  /**
   * Fetch configuration for a specific node
   */
  static async getNodeConfig(algorithm = 'Algorithm1', layer, node) {
    try {
      const response = await axios.get(`${BASE_URL}/config/node`, {
        params: { algorithm, layer, node }
      });
      return response.data;
    } catch (error) {
      throw new Error(`Failed to fetch node configuration: ${error.message}`);
    }
  }

  /**
   * Update node configuration
   */
  static async updateNodeConfig(layer, name, key, value) {
    try {
      const response = await axios.post(`${BASE_URL}/config/node`, {
        layer,
        name,
        key,
        value
      });
      
      if (response.status !== 200) {
        throw new Error(response.data.error || 'Update failed');
      }
      
      return response.data;
    } catch (error) {
      throw new Error(`Failed to update node configuration: ${error.message}`);
    }
  }
} 