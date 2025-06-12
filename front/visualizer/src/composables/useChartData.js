import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export function useChartData() {
  const layers = ref([])
  const chartData = ref([])
  const nodeCounts = ref([])

  const fetchLayers = async () => {
    try {
      const response = await axios.get('http://localhost:8889/layer', {
        params: {
          algorithm: 'Algorithm1'
        }
      })
      layers.value = response.data
      return response.data
    } catch (error) {
      ElMessage.error('Failed to fetch layer information: ' + error.message)
      return []
    }
  }

  const fetchNodes = async (layer) => {
    try {
      const response = await axios.get('http://localhost:8889/node', {
        params: {
          algorithm: 'Algorithm1',
          layer: layer
        }
      })
      return response.data.length
    } catch (error) {
      ElMessage.error('Failed to fetch node information: ' + error.message)
      return 0
    }
  }

  const buildChartData = async () => {
    const layerList = await fetchLayers()
    const data = []
    const counts = []

    for (const layer of layerList) {
      const nodeCount = await fetchNodes(layer)
      counts.push(nodeCount)
      data.push({ value: nodeCount, name: layer })
    }

    nodeCounts.value = counts
    chartData.value = data
    return data
  }

  const refreshData = async () => {
    await buildChartData()
  }

  return {
    layers,
    chartData,
    nodeCounts,
    fetchLayers,
    fetchNodes,
    buildChartData,
    refreshData
  }
} 