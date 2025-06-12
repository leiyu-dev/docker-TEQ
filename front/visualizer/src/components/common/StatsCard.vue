<template>
  <el-card class="stats-card hover-lift">
    <template #header>
      <div class="card-header">
        <span class="card-title">Runtime Statistics</span>
        <el-icon class="card-icon"><DataAnalysis /></el-icon>
      </div>
    </template>
    <div class="stats-content">
      <div class="stat-item">
        <div class="stat-value">{{ layers }}</div>
        <div class="stat-label">Running Layers</div>
        <div class="stat-icon layers-icon">
          <el-icon><Grid /></el-icon>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ nodes }}</div>
        <div class="stat-label">Running Nodes</div>
        <div class="stat-icon nodes-icon">
          <el-icon><Connection /></el-icon>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script>
import { DataAnalysis, Grid, Connection } from '@element-plus/icons-vue'

export default {
  name: 'StatsCard',
  components: {
    DataAnalysis,
    Grid,
    Connection
  },
  props: {
    layers: {
      type: Number,
      default: 0
    },
    nodes: {
      type: Number,
      default: 0
    }
  }
};
</script>

<style scoped>
.stats-card {
  min-height: 320px;
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

.card-icon {
  font-size: 20px;
  color: var(--primary-color);
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
  height: 100%;
  justify-content: center;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.1), rgba(245, 87, 108, 0.1));
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.stat-item:hover::before {
  transform: translateX(100%);
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--primary-color);
  margin-right: 20px;
}

.stat-label {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-icon {
  margin-left: auto;
  font-size: 32px;
  opacity: 0.6;
}

.layers-icon {
  color: #667eea;
}

.nodes-icon {
  color: #764ba2;
}
</style> 