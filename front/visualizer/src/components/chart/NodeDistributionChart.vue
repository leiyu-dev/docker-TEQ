<template>
  <el-card class="chart-card hover-lift">
    <template #header>
      <div class="card-header">
        <span class="card-title">Node Distribution</span>
        <el-icon class="card-icon"><PieChart /></el-icon>
      </div>
    </template>
    <div class="chart-container">
      <div ref="chartRef" class="pie-chart"></div>
    </div>
  </el-card>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

export default {
  name: 'NodeDistributionChart',
  components: {
    PieChart
  },
  props: {
    chartData: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const chartRef = ref(null)
    let chart = null

    const initChart = () => {
      if (chartRef.value) {
        chart = echarts.init(chartRef.value, null, { renderer: 'svg' })
        updateChart()
      }
    }

    const updateChart = () => {
      if (!chart) return

      const option = {
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
            data: props.chartData,
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
      }

      chart.setOption(option)
    }

    onMounted(() => {
      initChart()
    })

    watch(() => props.chartData, () => {
      updateChart()
    }, { deep: true })

    return {
      chartRef
    }
  }
}
</script>

<style scoped>
.chart-card {
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

.card-icon {
  font-size: 20px;
  color: var(--primary-color);
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
</style> 