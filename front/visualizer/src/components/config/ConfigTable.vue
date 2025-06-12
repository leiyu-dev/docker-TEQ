<!--
  ConfigTable Component
  Displays configuration parameters in a table format with editing capabilities
-->
<template>
  <div class="config-content slide-in-up" style="animation-delay: 0.2s;">
    <el-card class="config-card hover-lift" v-if="configNodes.length">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="config-title">Node Configuration Parameters</span>
            <span class="config-count">{{ configNodes.length }} parameters</span>
            <el-tag 
              v-if="showAllNodesNote" 
              type="warning" 
              size="small"
            >
              Showing values from node 0
            </el-tag>
          </div>
          <div class="header-right">
            <el-tag type="success" size="small">Node Config</el-tag>
          </div>
        </div>
      </template>

      <el-table 
        :data="configNodes" 
        class="config-table"
        :header-cell-style="{ backgroundColor: '#f8fafc', color: '#2d3748' }"
      >
        <el-table-column prop="key" label="Parameter Name" width="300px">
          <template #default="scope">
            <div class="param-name">
              <el-icon class="param-icon"><Setting /></el-icon>
              <span>{{ scope.row.key }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Current Value" width="250px">
          <template #default="scope">
            <div class="current-value">
              <el-tag 
                :type="getValueType(scope.row.value)" 
                effect="light"
                size="small"
              >
                {{ scope.row.value }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Modify Configuration" min-width="500px">
          <template #default="scope">
            <div class="modify-section">
              <el-input 
                v-model="scope.row.newValue" 
                placeholder="Enter new configuration value"
                class="new-value-input"
                clearable
              />
              <el-button
                  type="primary"
                  @click="handleUpdate(scope.row.key, scope.row.newValue)"
                  class="update-btn"
                  :disabled="!hasValueChanged(scope.row.value, scope.row.newValue)"
                  :loading="updating === scope.row.key"
              >
                <el-icon><Check /></el-icon>
                Update
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Empty State -->
    <el-card class="empty-card hover-lift" v-else>
      <el-empty 
        description="Please select layer and node to view configuration parameters"
        :image-size="120"
      >
        <template #image>
          <div class="empty-icon">
            <el-icon :size="80"><Connection /></el-icon>
          </div>
        </template>
        <template #description>
          <span class="empty-text">Please select layer and node</span>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script>
import { ConfigUtils } from '@/utils/configUtils.js';

export default {
  name: 'ConfigTable',
  props: {
    configNodes: {
      type: Array,
      default: () => []
    },
    selectedNode: {
      type: String,
      default: null
    }
  },
  emits: ['update-config'],
  data() {
    return {
      updating: null
    };
  },
  computed: {
    showAllNodesNote() {
      return this.selectedNode === 'All nodes';
    }
  },
  methods: {
    getValueType(value) {
      return ConfigUtils.getValueType(value);
    },
    hasValueChanged(originalValue, newValue) {
      return ConfigUtils.hasValueChanged(originalValue, newValue);
    },
    async handleUpdate(key, newValue) {
      this.updating = key;
      try {
        await this.$emit('update-config', key, newValue);
      } finally {
        this.updating = null;
      }
    }
  }
};
</script>

<style scoped>
.config-content {
  margin-bottom: 20px;
}

.config-card,
.empty-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.config-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.config-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.header-right {
  display: flex;
  align-items: center;
}

.config-table {
  margin-top: 20px;
}

.param-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.param-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.current-value {
  display: flex;
  align-items: center;
}

.modify-section {
  display: flex;
  gap: 12px;
  align-items: center;
}

.new-value-input {
  flex: 1;
  min-width: 200px;
}

.update-btn {
  white-space: nowrap;
  font-weight: 500;
}

.update-btn:disabled {
  opacity: 0.5;
}

.empty-card {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: var(--primary-color);
  margin: 0 auto 20px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Table styles */
.config-table :deep(.el-table__header) {
  background: linear-gradient(135deg, #f8fafc, #edf2f7);
}

.config-table :deep(.el-table__row:hover) {
  background-color: rgba(102, 126, 234, 0.04);
}

.config-table :deep(.el-table__cell) {
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 16px 12px;
}

/* Responsive design */
@media (max-width: 1200px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .modify-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .new-value-input {
    min-width: unset;
  }
}
</style> 