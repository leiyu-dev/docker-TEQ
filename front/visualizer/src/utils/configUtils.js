/**
 * Configuration Utilities
 * Helper functions for configuration handling
 */
export class ConfigUtils {
  /**
   * Get the appropriate tag type based on value type
   */
  static getValueType(value) {
    if (typeof value === 'boolean') return 'success';
    if (typeof value === 'number') return 'warning';
    if (typeof value === 'string') {
      const lowerValue = value.toLowerCase();
      if (lowerValue === 'true' || lowerValue === 'false') return 'success';
      if (!isNaN(value)) return 'warning';
    }
    return 'info';
  }

  /**
   * Transform configuration object to array format for table display
   */
  static transformConfigToArray(configData) {
    const configArray = [];
    for (const key in configData) {
      configArray.push({
        key: key,
        value: configData[key],
        newValue: configData[key]
      });
    }
    return configArray;
  }

  /**
   * Validate configuration value
   */
  static validateConfigValue(value) {
    if (value === null || value === undefined) return false;
    if (typeof value === 'string' && value.trim() === '') return false;
    return true;
  }

  /**
   * Check if configuration value has changed
   */
  static hasValueChanged(originalValue, newValue) {
    return newValue !== originalValue && this.validateConfigValue(newValue);
  }

  /**
   * Get display name for node
   */
  static getNodeDisplayName(node) {
    return node === 'All nodes' ? 'All Nodes' : node;
  }

  /**
   * Get the actual node name to send to API
   */
  static getApiNodeName(selectedNode, availableNodes) {
    if (selectedNode === 'All nodes' && availableNodes.length > 0) {
      return availableNodes[0];
    }
    return selectedNode;
  }
} 