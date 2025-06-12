<template>
  <div class="metrics-grid">
    <el-card class="metric-card hover-lift">
      <div class="metric-content">
        <div class="metric-icon memory-icon">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ memoryUsage }}</div>
          <div class="metric-label">Memory Usage</div>
        </div>
      </div>
    </el-card>

    <el-card class="metric-card hover-lift">
      <div class="metric-content">
        <div class="metric-icon cpu-icon">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ cpuUsage }}</div>
          <div class="metric-label">CPU Usage</div>
        </div>
      </div>
    </el-card>

    <el-card class="metric-card hover-lift uptime-card">
      <div class="metric-content">
        <div class="metric-icon uptime-icon">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="metric-info">
          <div class="metric-value">{{ uptime }}</div>
          <div class="metric-label">Uptime</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { Monitor, Cpu, Timer } from '@element-plus/icons-vue'

export default {
  name: 'MetricsGrid',
  components: {
    Monitor,
    Cpu,
    Timer
  },
  props: {
    memoryUsage: {
      type: String,
      default: '0%'
    },
    cpuUsage: {
      type: String,
      default: '0%'
    },
    uptime: {
      type: String,
      default: '0s'
    }
  }
}
</script>

<style scoped>
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 20px;
  height: 400px;
}

.metric-card {
  min-height: unset;
}

.uptime-card {
  grid-column: 1 / -1;
}

.metric-content {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  text-align: center;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 15px;
}

.memory-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.cpu-icon {
  background: linear-gradient(135deg, #fa709a, #fee140);
  color: white;
}

.uptime-icon {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  color: #667eea;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(3, 1fr);
  }
  
  .uptime-card {
    grid-column: 1;
  }
}
</style> 