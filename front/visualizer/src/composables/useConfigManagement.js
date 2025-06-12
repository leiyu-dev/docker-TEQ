/**
 * Configuration Management Composable
 * Handles all configuration-related state and operations
 */

import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ConfigService } from '@/services/configService.js';

export function useConfigManagement(chooseStore) {
  // Reactive state
  const options = ref([]);
  const configDetails = reactive([]);
  const isLoading = ref(false);

  /**
   * Transform config names to select options format
   * @param {string[]} names - Array of configuration names
   * @returns {Object[]} Array of select options
   */
  const transformToOptions = (names) => {
    return names.map((name) => ({ 
      value: name, 
      label: name 
    }));
  };

  /**
   * Transform config details to table format
   * @param {Object} details - Configuration details object
   * @returns {Object[]} Array of config parameters
   */
  const transformConfigDetails = (details) => {
    const result = [];
    for (const key in details) {
      result.push({
        key: key,
        value: details[key],
        newValue: details[key], // Initialize new value as current value
      });
    }
    return result;
  };

  /**
   * Fetch all available configuration options
   */
  const fetchOptions = async () => {
    try {
      isLoading.value = true;
      const data = await ConfigService.getConfigNames();
      options.value = transformToOptions(data);
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Fetch configuration details for selected config
   * @param {string} selected - Selected configuration name
   */
  const fetchConfigDetails = async (selected) => {
    if (!selected) return;

    try {
      isLoading.value = true;
      const data = await ConfigService.getConfigDetails(selected);
      
      // Clear previous data and populate new data
      configDetails.length = 0;
      const newDetails = transformConfigDetails(data);
      configDetails.push(...newDetails);
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Update a specific configuration parameter
   * @param {string} key - Parameter key
   * @param {*} newValue - New value
   * @param {string} selectedName - Selected configuration name
   */
  const updateConfigParameter = async (key, newValue, selectedName) => {
    try {
      isLoading.value = true;
      await ConfigService.updateConfigParameter(selectedName, key, newValue);
      
      // Refresh config details after successful update
      await fetchConfigDetails(selectedName);
      ElMessage.success('Configuration updated successfully!');
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Get value type for styling purposes
   * @param {*} value - The value to check
   * @returns {string} Tag type for styling
   */
  const getValueType = (value) => {
    if (typeof value === 'boolean') return 'success';
    if (typeof value === 'number') return 'warning';
    if (typeof value === 'string') {
      if (value.toLowerCase() === 'true' || value.toLowerCase() === 'false') {
        return 'success';
      }
      if (!isNaN(value)) return 'warning';
    }
    return 'info';
  };

  /**
   * Save current configuration as default
   */
  const saveAsDefault = () => {
    // TODO: Implement actual save logic
    ElMessage.success('Configuration saved as default parameters!');
  };

  /**
   * Initialize the composable
   */
  const initialize = () => {
    fetchOptions();
    if (chooseStore.selectedConfig) {
      fetchConfigDetails(chooseStore.selectedConfig);
    }
  };

  // Initialize on mount
  onMounted(initialize);

  return {
    // State
    options,
    configDetails,
    isLoading,
    
    // Methods
    fetchConfigDetails,
    updateConfigParameter,
    getValueType,
    saveAsDefault,
  };
} 