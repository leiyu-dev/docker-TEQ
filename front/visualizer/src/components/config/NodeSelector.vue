<template>
  <div class="selector-section slide-in-up" style="animation-delay: 0.1s;">
    <el-card class="selector-card hover-lift">
      <template #header>
        <div class="card-header">
          <span class="selector-title">Node Selection</span>
          <el-tag type="warning" size="small">Real-time Effect</el-tag>
        </div>
      </template>
      
      <el-row :gutter="24">
        <el-col :span="8">
          <div class="selector-item">
            <label class="selector-label">
              <el-icon class="label-icon"><Collection /></el-icon>
              Select Layer
            </label>
            <el-select
                size="large"
                v-model="selectedLayer"
                placeholder="Please select layer"
                class="selector-input"
                @change="handleLayerChange"
                clearable
            >
              <el-option
                  v-for="layer in layers"
                  :key="layer"
                  :label="layer"
                  :value="layer"
              >
                <div class="option-item">
                  <el-icon><DataBoard /></el-icon>
                  <span>{{ layer }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="selector-item">
            <label class="selector-label">
              <el-icon class="label-icon"><Connection /></el-icon>
              Select Node
            </label>
            <el-select
                size="large"
                v-model="selectedNode"
                placeholder="Please select node"
                class="selector-input"
                @change="handleNodeChange"
                clearable
                :disabled="!selectedLayer"
            >
              <el-option
                  v-for="node in nodes"
                  :key="node"
                  :label="getNodeDisplayName(node)"
                  :value="node"
              >
                <div class="option-item">
                  <el-icon v-if="node === 'All nodes'"><Grid /></el-icon>
                  <el-icon v-else><Cpu /></el-icon>
                  <span>{{ getNodeDisplayName(node) }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
        </el-col>
        
        <el-col :span="8">
          <div class="status-info">
            <div class="status-item">
              <span class="status-label">Current Layer:</span>
              <el-tag v-if="selectedLayer" type="primary" size="small">
                {{ selectedLayer }}
              </el-tag>
              <span v-else class="status-empty">Not Selected</span>
            </div>
            <div class="status-item">
              <span class="status-label">Current Node:</span>
              <el-tag v-if="selectedNode" type="success" size="small">
                {{ getNodeDisplayName(selectedNode) }}
              </el-tag>
              <span v-else class="status-empty">Not Selected</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { ConfigUtils } from '@/utils/configUtils.js';

export default {
  name: 'NodeSelector',
  props: {
    layers: {
      type: Array,
      default: () => []
    },
    nodes: {
      type: Array,
      default: () => []
    },
    modelValue: {
      type: Object,
      default: () => ({ selectedLayer: null, selectedNode: null })
    }
  },
  emits: ['update:modelValue', 'layer-change', 'node-change'],
  computed: {
    selectedLayer: {
      get() {
        return this.modelValue.selectedLayer;
      },
      set(value) {
        this.$emit('update:modelValue', { ...this.modelValue, selectedLayer: value });
      }
    },
    selectedNode: {
      get() {
        return this.modelValue.selectedNode;
      },
      set(value) {
        this.$emit('update:modelValue', { ...this.modelValue, selectedNode: value });
      }
    }
  },
  methods: {
    getNodeDisplayName(node) {
      return ConfigUtils.getNodeDisplayName(node);
    },
    handleLayerChange(layer) {
      this.$emit('layer-change', layer);
    },
    handleNodeChange(node) {
      this.$emit('node-change', node);
    }
  }
};
</script>

<style scoped>
.selector-section {
  margin-bottom: 30px;
}

.selector-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.selector-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.selector-item {
  margin-bottom: 20px;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.label-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.selector-input {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.status-empty {
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.6;
}

.selector-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.selector-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

@media (max-width: 768px) {
  .status-info {
    padding: 12px;
  }
  
  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .selector-item {
    margin-bottom: 16px;
  }
}
</style> 