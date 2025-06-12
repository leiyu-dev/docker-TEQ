<template>
  <el-card class="logs-card hover-lift">
    <template #header>
      <div class="card-header">
        <span class="card-title">System Logs</span>
        <div class="logs-actions">
          <el-button size="small" type="primary" @click="$emit('clear')">
            <el-icon><Delete /></el-icon>
            Clear
          </el-button>
          <el-button size="small" @click="$emit('refresh')">
            <el-icon><Refresh /></el-icon>
            Refresh
          </el-button>
        </div>
      </div>
    </template>
    <div class="logs-container">
      <div class="logs-wrapper custom-scrollbar">
        <pre class="logs-content">{{ logs || 'No log information available...' }}</pre>
      </div>
    </div>
  </el-card>
</template>

<script>
import { Delete, Refresh } from '@element-plus/icons-vue'

export default {
  name: 'LogsCard',
  components: {
    Delete,
    Refresh
  },
  props: {
    logs: {
      type: String,
      default: ''
    }
  },
  emits: ['clear', 'refresh']
}
</script>

<style scoped>
.logs-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.logs-actions {
  display: flex;
  gap: 8px;
}

.logs-container {
  height: 320px;
  background: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
}

.logs-wrapper {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.logs-content {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #e2e8f0;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style> 