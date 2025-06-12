<!--
  ConfigHeader Component
  Displays the page header with title, description, and action controls
-->
<template>
  <div class="page-header slide-in-up">
    <div class="header-content">
      <div class="title-section">
        <h1 class="page-title gradient-text">Global Configuration Management</h1>
        <p class="page-subtitle">
          Manage system global parameter configurations. Runtime modifications may cause undefined behavior.
        </p>
      </div>
      <div class="header-actions">
        <el-select
          :value="selectedConfig"
          placeholder="Select Configuration Class"
          size="large"
          class="config-selector"
          :loading="isLoading"
          @change="$emit('config-changed', $event)"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button 
          type="primary" 
          size="large" 
          @click="$emit('save-default')" 
          class="save-btn glow-on-hover"
          :disabled="!selectedConfig"
          :loading="isLoading"
        >
          <el-icon><DocumentAdd /></el-icon>
          Save as Default Parameters
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConfigHeader',
  props: {
    selectedConfig: {
      type: String,
      default: null
    },
    options: {
      type: Array,
      default: () => []
    },
    isLoading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['config-changed', 'save-default']
};
</script>

<style scoped>
.page-header {
  margin-bottom: 30px;
}

.header-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title-section {
  margin-bottom: 25px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
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

.header-actions {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.config-selector {
  min-width: 300px;
  flex: 1;
}

.save-btn {
  white-space: nowrap;
  font-weight: 600;
}

/* Responsive design */
@media (max-width: 768px) {
  .header-content {
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .config-selector {
    min-width: unset;
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
}
</style> 