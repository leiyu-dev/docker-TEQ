/**
 * Configuration Service
 * Handles all API calls related to configuration management
 */

const BASE_URL = 'http://localhost:8889';

export class ConfigService {
  /**
   * Fetch all available configuration names
   * @returns {Promise<string[]>} Array of configuration names
   */
  static async getConfigNames() {
    const response = await fetch(`${BASE_URL}/config/name`);
    
    if (response.status !== 200) {
      throw new Error('Failed to fetch configurations');
    }
    
    return response.json();
  }

  /**
   * Fetch detailed configuration parameters for a specific config
   * @param {string} configName - The name of the configuration
   * @returns {Promise<Object>} Configuration details
   */
  static async getConfigDetails(configName) {
    if (!configName) {
      throw new Error('Configuration name is required');
    }

    const response = await fetch(`${BASE_URL}/config/detail?name=${configName}`);
    
    if (response.status !== 200) {
      throw new Error(response.statusText);
    }
    
    return response.json();
  }

  /**
   * Update a specific configuration parameter
   * @param {string} configName - The configuration name
   * @param {string} key - The parameter key
   * @param {*} value - The new value
   * @returns {Promise<void>}
   */
  static async updateConfigParameter(configName, key, value) {
    const response = await fetch(`${BASE_URL}/config`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ 
        name: configName, 
        key, 
        value 
      }),
    });

    if (response.status !== 200) {
      throw new Error('Failed to update configuration');
    }
  }
} 