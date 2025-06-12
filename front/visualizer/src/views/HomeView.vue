<template>
  <!-- First row: System status overview -->
  <div class="status-overview slide-in-up">
    <el-row :gutter="30">
      <!-- Status card -->
      <el-col :span="12">
        <StatusCard 
          :status="statusStore.status" 
          @start="start"
          @stop="stop"
          @restart="restart"
        />
      </el-col>

      <!-- Runtime statistics -->
      <el-col :span="12">
        <StatsCard 
          :layers="statusStore.layers"
          :nodes="statusStore.nodes"
        />
      </el-col>
    </el-row>
  </div>

  <!-- Second row: Detailed information and charts -->
  <div class="details-section slide-in-up" style="animation-delay: 0.2s;">
    <el-row :gutter="30">
      <!-- Node distribution chart -->
      <el-col :span="12">
        <NodeDistributionChart :chart-data="chartData" />
      </el-col>

      <!-- System metrics -->
      <el-col :span="12">
        <MetricsGrid 
          :memory-usage="statusStore.memoryUsage"
          :cpu-usage="statusStore.cpuUsage"
          :uptime="statusStore.uptime"
        />
      </el-col>
    </el-row>
  </div>

  <!-- Third row: Logs area -->
  <div class="logs-section slide-in-up" style="animation-delay: 0.4s;">
    <LogsCard 
      :logs="logsStore.logs"
      @clear="clearLogs"
      @refresh="refreshLogs"
    />
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useStatusStore } from '@/stores/status.js'
import { useLogsStore } from '@/stores/logs.js'
import { useSimulatorControl } from '@/composables/useSimulatorControl.js'
import { useChartData } from '@/composables/useChartData.js'

// Component imports
import StatusCard from '@/components/common/StatusCard.vue'
import StatsCard from '@/components/common/StatsCard.vue'
import NodeDistributionChart from '@/components/chart/NodeDistributionChart.vue'
import MetricsGrid from '@/components/common/MetricsGrid.vue'
import LogsCard from '@/components/common/LogsCard.vue'

export default {
  name: 'HomeView',
  components: {
    StatusCard,
    StatsCard,
    NodeDistributionChart,
    MetricsGrid,
    LogsCard
  },
  setup() {
    const statusStore = useStatusStore()
    const logsStore = useLogsStore()
    
    // Use composables
    const { start, stop, restart } = useSimulatorControl(statusStore)
    const { chartData, buildChartData } = useChartData()

    // Log management
    const clearLogs = () => {
      logsStore.logs = ''
      ElMessage.success('Logs cleared')
    }

    const refreshLogs = () => {
      ElMessage.success('Logs refreshed')
    }

    // Initialize data on mount
    onMounted(async () => {
      await buildChartData()
    })

    return {
      statusStore,
      logsStore,
      chartData,
      start,
      stop,
      restart,
      clearLogs,
      refreshLogs
    }
  }
}
</script>

<style scoped>
.status-overview,
.details-section,
.logs-section {
  margin-bottom: 40px;
}

/* Animation styles */
.slide-in-up {
  animation: slideInUp 0.6s ease-out forwards;
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

/* Global hover effect */
.hover-lift {
  transition: all 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

/* Glow effect for buttons */
.glow-on-hover {
  position: relative;
  overflow: hidden;
}

.glow-on-hover::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.glow-on-hover:hover::before {
  left: 100%;
}

/* Custom scrollbar */
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

/* Responsive design */
@media (max-width: 768px) {
  .status-overview,
  .details-section,
  .logs-section {
    margin-bottom: 20px;
  }
}
</style>

