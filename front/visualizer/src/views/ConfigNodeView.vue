<template>
  <div class="config-node-container fade-in">
    <!-- Page Header -->
    <div class="page-header slide-in-up">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title gradient-text">Node Configuration Management</h1>
          <p class="page-subtitle">Configure node parameters in real-time, all changes take effect immediately</p>
        </div>
      </div>
    </div>

    <!-- Node Selector -->
    <NodeSelector
      :layers="layers"
      :nodes="nodes"
      v-model="selectorModel"
      @layer-change="handleLayerChange"
      @node-change="handleNodeChange"
    />

    <!-- Configuration Table -->
    <ConfigTable
      :config-nodes="configNodes"
      :selected-node="selectorModel.selectedNode"
      @update-config="handleUpdateConfig"
    />
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useChooseStore } from '@/stores/choose.js';
import { NodeConfigService } from '@/services/nodeConfigService.js';
import { ConfigUtils } from '@/utils/configUtils.js';
import NodeSelector from '@/components/config/NodeSelector.vue';
import ConfigTable from '@/components/config/ConfigTable.vue';

export default {
  name: 'ConfigNodeView',
  components: {
    NodeSelector,
    ConfigTable
  },
  setup() {
    // Reactive data
    const layers = ref([]);
    const nodes = ref([]);
    const configNodes = ref([]);
    const chooseStore = useChooseStore();
    
    // Selector model for two-way binding with child component
    const selectorModel = reactive({
      selectedLayer: chooseStore.selectedLayer,
      selectedNode: chooseStore.selectedNode
    });

    // Methods
    const fetchData = async () => {
      try {
        await Promise.all([
          fetchLayers(),
          // Initialize nodes and config if layer is already selected
          selectorModel.selectedLayer && fetchNodes(),
          selectorModel.selectedLayer && selectorModel.selectedNode && fetchConfigNodes()
        ].filter(Boolean));
      } catch (error) {
        ElMessage.error(`Failed to initialize data: ${error.message}`);
      }
    };

    const fetchLayers = async () => {
      try {
        layers.value = await NodeConfigService.getLayers();
      } catch (error) {
        ElMessage.error(error.message);
      }
    };

    const fetchNodes = async () => {
      if (!selectorModel.selectedLayer) return;
      
      try {
        nodes.value = await NodeConfigService.getNodes('Algorithm1', selectorModel.selectedLayer);
      } catch (error) {
        ElMessage.error(error.message);
      }
    };

    const fetchConfigNodes = async () => {
      if (!selectorModel.selectedLayer || !selectorModel.selectedNode) return;

      try {
        const actualNode = ConfigUtils.getApiNodeName(selectorModel.selectedNode, nodes.value);
        const configData = await NodeConfigService.getNodeConfig(
          'Algorithm1',
          selectorModel.selectedLayer,
          actualNode
        );
        
        configNodes.value = ConfigUtils.transformConfigToArray(configData);
      } catch (error) {
        ElMessage.error(error.message);
      }
    };

    const handleLayerChange = async (layer) => {
      // Update store
      chooseStore.selectedLayer = layer;
      chooseStore.selectedNode = null;
      
      // Update local model
      selectorModel.selectedLayer = layer;
      selectorModel.selectedNode = null;
      
      // Clear dependent data
      nodes.value = [];
      configNodes.value = [];
      
      // Fetch new data if layer is selected
      if (layer) {
        await fetchNodes();
      }
    };

    const handleNodeChange = async (node) => {
      // Update store
      chooseStore.selectedNode = node;
      
      // Update local model
      selectorModel.selectedNode = node;
      
      // Clear config data
      configNodes.value = [];
      
      // Fetch new config if node is selected
      if (node) {
        await fetchConfigNodes();
      }
    };

    const handleUpdateConfig = async (key, newValue) => {
      try {
        await NodeConfigService.updateNodeConfig(
          selectorModel.selectedLayer,
          selectorModel.selectedNode,
          key,
          newValue
        );
        
        ElMessage.success('Node configuration updated successfully');
        
        // Refresh configuration data
        await fetchConfigNodes();
      } catch (error) {
        ElMessage.error(error.message);
      }
    };

    // Lifecycle
    onMounted(fetchData);

    return {
      layers,
      nodes,
      configNodes,
      selectorModel,
      handleLayerChange,
      handleNodeChange,
      handleUpdateConfig
    };
  }
};
</script>

<style>
/* Config Node View Common Styles */

:root {
  --text-primary: #2d3748;
  --text-secondary: #718096;
  --primary-color: #667eea;
  --gradient-start: #667eea;
  --gradient-end: #764ba2;
  --shadow-light: rgba(0, 0, 0, 0.08);
  --shadow-medium: rgba(0, 0, 0, 0.15);
  --border-light: rgba(102, 126, 234, 0.1);
  --bg-light: rgba(102, 126, 234, 0.05);
}

/* Common Container Styles */
.config-node-container {
  min-height: 100vh;
  padding: 20px;
}

/* Page Header Styles */
.page-header {
  margin-bottom: 30px;
}

.header-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px var(--shadow-light);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title-section {
  text-align: left;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 400;
}

/* Animation Classes */
.fade-in {
  animation: fadeIn 0.8s ease-out;
}

.slide-in-up {
  animation: slideInUp 0.6s ease-out;
}

.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px var(--shadow-medium);
}

.gradient-text {
  background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Keyframe Animations */
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

/* Responsive Design */
@media (max-width: 768px) {
  .config-node-container {
    padding: 15px;
  }
  
  .header-content {
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 15px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
} </style>
