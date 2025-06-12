<!--
  ConfigGlobalView - Global Configuration Management
  Main view component for managing system global parameter configurations
-->
<script>
import { useChooseStore } from '@/stores/choose.js';
import { useConfigManagement } from '@/composables/useConfigManagement.js';
import ConfigHeader from '@/components/config/ConfigHeader.vue';
import ConfigTable from '@/components/config/ConfigTable.vue';
import ConfigEmptyState from '@/components/config/ConfigEmptyState.vue';

export default {
  name: 'ConfigGlobalView',
  components: {
    ConfigHeader,
    ConfigTable,
    ConfigEmptyState,
  },
  setup() {
    // Store and composable setup
    const chooseStore = useChooseStore();
    const {
      options,
      configDetails,
      isLoading,
      fetchConfigDetails,
      updateConfigParameter,
      getValueType,
      saveAsDefault,
    } = useConfigManagement(chooseStore);

    // Event handlers
    const handleConfigChanged = (selectedConfig) => {
      chooseStore.selectedConfig = selectedConfig;
      fetchConfigDetails(selectedConfig);
    };

    const handleUpdateParameter = ({ key, value, configName }) => {
      updateConfigParameter(key, value, configName);
    };

    const handleSaveDefault = () => {
      saveAsDefault();
    };

    return {
      // Store
      chooseStore,
      
      // State
      options,
      configDetails,
      isLoading,
      
      // Methods
      getValueType,
      
      // Event handlers
      handleConfigChanged,
      handleUpdateParameter,
      handleSaveDefault,
    };
  },
};
</script>

<template>
  <div class="config-global-container fade-in">
    <!-- Page Header with Configuration Selection -->
    <ConfigHeader
      :selected-config="chooseStore.selectedConfig"
      :options="options"
      :is-loading="isLoading"
      @config-changed="handleConfigChanged"
      @save-default="handleSaveDefault"
    />

    <!-- Configuration Content -->
    <div class="config-content slide-in-up" style="animation-delay: 0.2s;">
      <!-- Configuration Table -->
      <ConfigTable
        v-if="configDetails.length > 0"
        :config-name="chooseStore.selectedConfig"
        :config-details="configDetails"
        :is-loading="isLoading"
        :get-value-type="getValueType"
        @update-parameter="handleUpdateParameter"
      />

      <!-- Empty State -->
      <ConfigEmptyState v-else />
    </div>
  </div>
</template>

<style scoped>
.config-global-container {
  min-height: 100vh;
  padding: 20px;
}

.config-content {
  margin-bottom: 20px;
}

/* Animation classes */
.fade-in {
  animation: fadeIn 0.6s ease-out;
}

.slide-in-up {
  animation: slideInUp 0.6s ease-out;
}

.hover-lift {
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
}

.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.glow-on-hover {
  transition: box-shadow 0.3s ease;
}

.glow-on-hover:hover {
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.3);
}

.gradient-text {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Keyframe animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .config-global-container {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .config-global-container {
    padding: 10px;
  }
}
</style>
