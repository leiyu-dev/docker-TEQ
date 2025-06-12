<template>
  <!-- 第一行：系统状态概览 -->
  <div class="status-overview slide-in-up">
    <el-row :gutter="30">
      <!-- 状态卡片 -->
      <el-col :span="12">
        <el-card class="status-card hover-lift" :class="statusClass">
          <template #header>
            <div class="card-header">
              <span class="card-title">System Status</span>
              <div class="status-indicator" :class="statusStore.status.toLowerCase()"></div>
            </div>
          </template>
          <div class="status-content">
            <div class="status-display">
              <div class="status-icon-wrapper" :class="statusStore.status.toLowerCase()">
                <el-icon :size="60" class="status-icon">
                  <DArrowRight v-if="statusStore.status==='RUNNING'" />
                  <Refresh v-else-if="statusStore.status==='RESTARTING' || statusStore.status==='STOPPING'" />
                  <CircleClose v-else-if="statusStore.status==='STOPPED'" />
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
                @click="start()" 
                class="control-btn glow-on-hover"
                :disabled="isActionDisabled('start')"
              >
                <el-icon><VideoPlay /></el-icon>
                Start
              </el-button>
              <el-button 
                type="danger" 
                size="large" 
                @click="stop()" 
                class="control-btn glow-on-hover"
                :disabled="isActionDisabled('stop')"
              >
                <el-icon><VideoPause /></el-icon>
                Stop
              </el-button>
              <el-button 
                type="primary" 
                size="large" 
                @click="restart()" 
                class="control-btn glow-on-hover"
                :disabled="isActionDisabled('restart')"
              >
                <el-icon><Refresh /></el-icon>
                Restart
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 运行统计 -->
      <el-col :span="12">
        <el-card class="stats-card hover-lift">
          <template #header>
            <div class="card-header">
              <span class="card-title">Runtime Statistics</span>
              <el-icon class="card-icon"><DataAnalysis /></el-icon>
            </div>
          </template>
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-value">{{ statusStore.layers }}</div>
              <div class="stat-label">Running Layers</div>
              <div class="stat-icon layers-icon">
                <el-icon><Stack /></el-icon>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statusStore.nodes }}</div>
              <div class="stat-label">Running Nodes</div>
              <div class="stat-icon nodes-icon">
                <el-icon><Connection /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <!-- 第二行：详细信息和图表 -->
  <div class="details-section slide-in-up" style="animation-delay: 0.2s;">
    <el-row :gutter="30">
      <!-- 节点分布图表 -->
      <el-col :span="12">
        <el-card class="chart-card hover-lift">
          <template #header>
            <div class="card-header">
              <span class="card-title">Node Distribution</span>
              <el-icon class="card-icon"><PieChart /></el-icon>
            </div>
          </template>
          <div class="chart-container">
            <div :ref="'chart'" class="pie-chart"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统指标 -->
      <el-col :span="12">
        <div class="metrics-grid">
          <el-card class="metric-card hover-lift">
            <div class="metric-content">
              <div class="metric-icon memory-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-value">{{ statusStore.memoryUsage }}</div>
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
                <div class="metric-value">{{ statusStore.cpuUsage }}</div>
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
                <div class="metric-value">{{ statusStore.uptime }}</div>
                <div class="metric-label">Uptime</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>

  <!-- 第三行：日志区域 -->
  <div class="logs-section slide-in-up" style="animation-delay: 0.4s;">
    <el-card class="logs-card hover-lift">
      <template #header>
        <div class="card-header">
          <span class="card-title">System Logs</span>
          <div class="logs-actions">
            <el-button size="small" type="primary" @click="clearLogs">
              <el-icon><Delete /></el-icon>
              Clear
            </el-button>
            <el-button size="small" @click="refreshLogs">
              <el-icon><Refresh /></el-icon>
              Refresh
            </el-button>
          </div>
        </div>
      </template>
      <div class="logs-container">
        <div class="logs-wrapper custom-scrollbar">
          <pre class="logs-content">{{ logsStore.logs || 'No log information available...' }}</pre>
        </div>
      </div>
    </el-card>
  </div>
  </template>
<script>
import {ElMessage} from "element-plus";
import {useStatusStore} from "@/stores/status.js";
import * as echarts from "echarts";
import axios from "axios";
import {useLogsStore} from "@/stores/logs.js";
export default {
  data() {
    return {
      statusStore : useStatusStore(),
      logsStore : useLogsStore(),
      layers : [],
      nodeCounts: [],
    };
  },
  computed: {
    statusClass() {
      return `status-${this.statusStore.status.toLowerCase()}`;
    }
  },
  async mounted() {
    const chartRef = this.$refs['chart'];
    const chart = echarts.init(chartRef, null, {renderer: 'svg'});
    await this.fetchLayers();
    let data = []
    for(let layer of this.layers){
      const nodeCount = await this.fetchNodes(layer);
      this.nodeCounts.push(nodeCount);
      data.push({value: nodeCount, name: layer});
    }
    let option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#667eea',
        borderWidth: 1,
        textStyle: {
          color: '#333'
        }
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'center',
        textStyle: {
          color: '#666',
          fontSize: 12
        }
      },
      series: [
        {
          name: 'Node Distribution',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          data: data,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ],
      color: ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe']
    };
    chart.setOption(option);
  },
  methods:{
    getStatusText() {
      const statusMap = {
        'RUNNING': 'Running',
        'STOPPED': 'Stopped',
        'RESTARTING': 'Restarting',
        'STOPPING': 'Stopping',
        'DISCONNECTED': 'Disconnected'
      };
      return statusMap[this.statusStore.status] || this.statusStore.status;
    },
    getStatusDescription() {
      const descMap = {
        'RUNNING': 'System is running normally',
        'STOPPED': 'System has stopped running',
        'RESTARTING': 'System is restarting',
        'STOPPING': 'System is stopping',
        'DISCONNECTED': 'Unable to connect to system'
      };
      return descMap[this.statusStore.status] || 'Status unknown';
    },
    isActionDisabled(action) {
      const status = this.statusStore.status;
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
    },
    clearLogs() {
      this.logsStore.logs = '';
      ElMessage.success('Logs cleared');
    },
    refreshLogs() {
      // Logic for refreshing logs
      ElMessage.success('Logs refreshed');
    },
    start(){
      if(this.statusStore.status === 'RUNNING'){
        ElMessage.warning('Simulator is already running');
        return;
      }
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('Simulator is restarting');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('Simulator connection is disconnected');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('Simulator is stopping');
        return;
      }
      //use post /start to start the simulator
      fetch('http://localhost:8889/start', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
      }).then(response => {
        if(response.ok){
          return response.json();
        }
        throw new Error('Network response was not ok');
      }).then(data => {
        if(data.code === 0){
          ElMessage.success('Simulator started successfully');
          this.statusStore.status = 'RUNNING';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('Start failed: ' + error.message);
      });
    },
    stop(){
      if(this.statusStore.status === 'STOPPED'){
        ElMessage.warning('Simulator is already stopped');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('Simulator connection is disconnected');
        return;
      }
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('Simulator is restarting');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('Simulator is stopping');
        return;
      }
      //use post /stop to stop the simulator
      fetch('http://localhost:8889/stop', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
      }).then(response => {
        if(response.ok){
          return response.json();
        }
        throw new Error('Network response was not ok');
      }).then(data => {
        if(data.code === 0){
          ElMessage.success('Simulator stopped');
          this.statusStore.status = 'STOPPED';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('Stop failed: ' + error.message);
      });
    },
    restart(){
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('Simulator is restarting');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('Simulator connection is disconnected');
        return;
      }
      if(this.statusStore.status === 'STOPPED'){
        ElMessage.warning('Simulator is stopped');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('Simulator is stopping');
        return;
      }
      //use post /restart to restart the simulator
      fetch('http://localhost:8889/restart', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
      }).then(response => {
        if(response.ok){
          return response.json();
        }
        throw new Error('Network response was not ok');
      }).then(data => {
        if(data.code === 0){
          ElMessage.success('Simulator restarting');
          this.statusStore.status = 'RESTARTING';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('Restart failed: ' + error.message);
      });
    },
    async fetchLayers() {
      try {
        const response = await axios.get("http://localhost:8889/layer", {
          params: {
            //todo: add algorithm
            algorithm: "Algorithm1",
          },
        });
        this.layers = response.data;
      } catch (error) {
        ElMessage.error("Failed to fetch layer information:", error);
      }
    },
    async fetchNodes(layer) {
      try {
        const response = await axios.get("http://localhost:8889/node", {
          params: {
            algorithm: "Algorithm1",
            layer: layer,
          },
        });
        return response.data.length;
      } catch (error) {
        ElMessage.error("Failed to fetch node information:", error);
      }
    },
  }
};
</script>
<style scoped>
.status-overview,
.details-section,
.logs-section {
  margin-bottom: 40px;
}

/* 状态卡片样式 */
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

.card-icon {
  font-size: 20px;
  color: var(--primary-color);
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

/* 统计卡片样式 */
.stats-card {
  min-height: 320px;
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

/* 图表卡片样式 */
.chart-card {
  min-height: 400px;
}

.chart-container {
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart {
  width: 100%;
  height: 100%;
}

/* 指标网格样式 */
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
  height: 100%;
  padding: 20px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 20px;
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
  font-size: 12px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* 日志卡片样式 */
.logs-card {
  min-height: 400px;
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

/* 动画效果 */
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

/* 响应式设计 */
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
  
  .metrics-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(3, 1fr);
  }
  
  .uptime-card {
    grid-column: 1;
  }
}
</style>

