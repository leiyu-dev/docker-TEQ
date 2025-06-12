<template>
  <el-card class="status-card hover-lift" :class="statusClass">
    <template #header>
      <div class="card-header">
        <span class="card-title">System Status</span>
        <div class="status-indicator" :class="status.toLowerCase()"></div>
      </div>
    </template>
    <div class="status-content">
      <div class="status-display">
        <div class="status-icon-wrapper" :class="status.toLowerCase()">
          <el-icon :size="60" class="status-icon">
            <DArrowRight v-if="status === 'RUNNING'" />
            <Refresh v-else-if="status === 'RESTARTING' || status === 'STOPPING'" />
            <CircleClose v-else-if="status === 'STOPPED'" />
            <Loading v-else />
          </el-icon>
        </div>
        <div class="status-text">
          <h3 class="status-title">{{ getStatusText() }}</h3>
          <p class="status-description">{{ getStatusDescription() }}</p>
        </div>
      </div>
      
      <div class="control-buttons">
        <el-button 
          type="success" 
          size="large" 
          @click="$emit('start')" 
          class="control-btn glow-on-hover"
          :disabled="isActionDisabled('start')"
        >
          <el-icon><VideoPlay /></el-icon>
          Start
        </el-button>
        <el-button 
          type="danger" 
          size="large" 
          @click="$emit('stop')" 
          class="control-btn glow-on-hover"
          :disabled="isActionDisabled('stop')"
        >
          <el-icon><VideoPause /></el-icon>
          Stop
        </el-button>
        <el-button 
          type="primary" 
          size="large" 
          @click="$emit('restart')" 
          class="control-btn glow-on-hover"
          :disabled="isActionDisabled('restart')"
        >
          <el-icon><Refresh /></el-icon>
          Restart
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script>
import { DArrowRight, Refresh, CircleClose, Loading, VideoPlay, VideoPause } from '@element-plus/icons-vue'

export default {
  name: 'StatusCard',
  components: {
    DArrowRight,
    Refresh,
    CircleClose,
    Loading,
    VideoPlay,
    VideoPause
  },
  props: {
    status: {
      type: String,
      required: true
    }
  },
  emits: ['start', 'stop', 'restart'],
  computed: {
    statusClass() {
      return `status-${this.status.toLowerCase()}`;
    }
  },
  methods: {
    getStatusText() {
      const statusMap = {
        'RUNNING': 'Running',
        'STOPPED': 'Stopped',
        'RESTARTING': 'Restarting',
        'STOPPING': 'Stopping',
        'DISCONNECTED': 'Disconnected'
      };
      return statusMap[this.status] || this.status;
    },
    getStatusDescription() {
      const descMap = {
        'RUNNING': 'System is running normally',
        'STOPPED': 'System has stopped',
        'RESTARTING': 'System is restarting',
        'STOPPING': 'System is stopping',
        'DISCONNECTED': 'Cannot connect to system'
      };
      return descMap[this.status] || 'Unknown status';
    },
    isActionDisabled(action) {
      const status = this.status;
      switch(action) {
        case 'start':
          return status === 'RUNNING' || status === 'RESTARTING' || status === 'DISCONNECTED' || status === 'STOPPING';
        case 'stop':
          return status === 'STOPPED' || status === 'DISCONNECTED' || status === 'RESTARTING' || status === 'STOPPING';
        case 'restart':
          return status === 'RESTARTING' || status === 'DISCONNECTED' || status === 'STOPPED' || status === 'STOPPING';
        default:
          return false;
      }
    }
  }
};
</script>

<style scoped>
.status-card {
  min-height: 320px;
}

.status-card.status-running {
  border-left: 4px solid #48bb78;
}

.status-card.status-stopped {
  border-left: 4px solid #f56565;
}

.status-card.status-restarting,
.status-card.status-stopping {
  border-left: 4px solid #ed8936;
}

.status-card.status-disconnected {
  border-left: 4px solid #a0aec0;
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

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-indicator.running {
  background: #48bb78;
}

.status-indicator.stopped {
  background: #f56565;
}

.status-indicator.restarting,
.status-indicator.stopping {
  background: #ed8936;
}

.status-indicator.disconnected {
  background: #a0aec0;
}

.status-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.status-display {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
}

.status-icon-wrapper {
  padding: 20px;
  border-radius: 50%;
  margin-right: 20px;
  transition: all 0.3s ease;
}

.status-icon-wrapper.running {
  background: linear-gradient(135deg, #48bb78, #38a169);
  color: white;
}

.status-icon-wrapper.stopped {
  background: linear-gradient(135deg, #f56565, #e53e3e);
  color: white;
}

.status-icon-wrapper.restarting,
.status-icon-wrapper.stopping {
  background: linear-gradient(135deg, #ed8936, #dd6b20);
  color: white;
}

.status-icon-wrapper.disconnected {
  background: linear-gradient(135deg, #a0aec0, #718096);
  color: white;
}

.status-text {
  flex: 1;
}

.status-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.status-description {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.control-buttons {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.control-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 currentColor;
  }
  70% {
    box-shadow: 0 0 0 10px rgba(102, 126, 234, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0);
  }
}

@media (max-width: 768px) {
  .status-display {
    flex-direction: column;
    text-align: center;
  }
  
  .status-icon-wrapper {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .control-buttons {
    flex-direction: column;
  }
}
</style> 