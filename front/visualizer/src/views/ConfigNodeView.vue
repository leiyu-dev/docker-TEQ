<template>
  <div class="config-node-container fade-in">
    <!-- 页面标题 -->
    <div class="page-header slide-in-up">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title gradient-text">节点配置管理</h1>
          <p class="page-subtitle">实时配置节点参数，所有修改将立即生效</p>
        </div>
      </div>
    </div>

    <!-- 选择器区域 -->
    <div class="selector-section slide-in-up" style="animation-delay: 0.1s;">
      <el-card class="selector-card hover-lift">
        <template #header>
          <div class="card-header">
            <span class="selector-title">节点选择</span>
            <el-tag type="warning" size="small">实时生效</el-tag>
          </div>
        </template>
        
        <el-row :gutter="24">
          <el-col :span="8">
            <div class="selector-item">
              <label class="selector-label">
                <el-icon class="label-icon"><Collection /></el-icon>
                选择层级
              </label>
              <el-select
                  size="large"
                  v-model="chooseStore.selectedLayer"
                  placeholder="请选择层级"
                  class="selector-input"
                  @change="fetchNodes"
                  clearable
              >
                <el-option
                    v-for="layer in layers"
                    :key="layer"
                    :label="layer"
                    :value="layer"
                >
                  <div class="option-item">
                    <el-icon><DataBoard /></el-icon>
                    <span>{{ layer }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
          </el-col>
          
          <el-col :span="8">
            <div class="selector-item">
              <label class="selector-label">
                <el-icon class="label-icon"><Connection /></el-icon>
                选择节点
              </label>
              <el-select
                  size="large"
                  v-model="chooseStore.selectedNode"
                  placeholder="请选择节点"
                  class="selector-input"
                  @change="fetchConfigNodes"
                  clearable
                  :disabled="!chooseStore.selectedLayer"
              >
                <el-option
                    v-for="node in nodes"
                    :key="node"
                    :label="node === 'All nodes' ? '所有节点' : node"
                    :value="node"
                >
                  <div class="option-item">
                    <el-icon v-if="node === 'All nodes'"><Grid /></el-icon>
                    <el-icon v-else><Cpu /></el-icon>
                    <span>{{ node === 'All nodes' ? '所有节点' : node }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
          </el-col>
          
          <el-col :span="8">
            <div class="status-info">
              <div class="status-item">
                <span class="status-label">当前层级：</span>
                <el-tag v-if="chooseStore.selectedLayer" type="primary" size="small">
                  {{ chooseStore.selectedLayer }}
                </el-tag>
                <span v-else class="status-empty">未选择</span>
              </div>
              <div class="status-item">
                <span class="status-label">当前节点：</span>
                <el-tag v-if="chooseStore.selectedNode" type="success" size="small">
                  {{ chooseStore.selectedNode === 'All nodes' ? '所有节点' : chooseStore.selectedNode }}
                </el-tag>
                <span v-else class="status-empty">未选择</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 配置表格区域 -->
    <div class="config-content slide-in-up" style="animation-delay: 0.2s;">
      <el-card class="config-card hover-lift" v-if="configNodes.length">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="config-title">节点配置参数</span>
              <span class="config-count">{{ configNodes.length }} 个参数</span>
              <el-tag 
                v-if="chooseStore.selectedNode === 'All nodes'" 
                type="warning" 
                size="small"
              >
                显示节点 0 的当前值
              </el-tag>
            </div>
            <div class="header-right">
              <el-tag type="success" size="small">节点配置</el-tag>
            </div>
          </div>
        </template>

        <el-table 
          :data="configNodes" 
          class="config-table"
          :header-cell-style="{ backgroundColor: '#f8fafc', color: '#2d3748' }"
        >
          <el-table-column prop="key" label="参数名称" width="300px">
            <template #default="scope">
              <div class="param-name">
                <el-icon class="param-icon"><Setting /></el-icon>
                <span>{{ scope.row.key }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="当前值" width="250px">
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
          
          <el-table-column label="修改配置" min-width="500px">
            <template #default="scope">
              <div class="modify-section">
                <el-input 
                  v-model="scope.row.newValue" 
                  placeholder="输入新的配置值"
                  class="new-value-input"
                  clearable
                />
                <el-button
                    type="primary"
                    @click="updateConfig(scope.row.key, scope.row.newValue, chooseStore.selectedNode)"
                    class="update-btn"
                    :disabled="!scope.row.newValue || scope.row.newValue === scope.row.value"
                >
                  <el-icon><Check /></el-icon>
                  更新
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 空状态 -->
      <el-card class="empty-card hover-lift" v-else>
        <el-empty 
          description="请选择层级和节点来查看配置参数"
          :image-size="120"
        >
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><Connection /></el-icon>
            </div>
          </template>
          <template #description>
            <span class="empty-text">请选择层级和节点</span>
          </template>
        </el-empty>
      </el-card>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import {ElMessage} from "element-plus";
import {useChooseStore} from "@/stores/choose.js";

export default {
  data() {
    return {
      algorithms: [],
      layers: [],
      nodes: [],
      configNodes: [],
      chooseStore: useChooseStore(),
    };
  },
  methods: {
    // 获取值类型对应的标签类型
    getValueType(value) {
      if (typeof value === 'boolean') return 'success';
      if (typeof value === 'number') return 'warning';
      if (typeof value === 'string') {
        if (value.toLowerCase() === 'true' || value.toLowerCase() === 'false') return 'success';
        if (!isNaN(value)) return 'warning';
      }
      return 'info';
    },
    async fetchAlgorithms() {
      try {
        const response = await axios.get("http://localhost:8889/algorithm");
        this.algorithms = response.data;
      } catch (error) {
        ElMessage.error("获取算法列表失败: " + error.message);
      }
    },
    async fetchLayers() {
      try {
        const response = await axios.get("http://localhost:8889/layer", {
          params: {
            algorithm: "Algorithm1",
          },
        });
        this.layers = response.data;
      } catch (error) {
        ElMessage.error("获取层级列表失败: " + error.message);
      }
    },
    async fetchNodes() {
      if (!"Algorithm1" || !this.chooseStore.selectedLayer) {
        return;
      }
      try {
        const response = await axios.get("http://localhost:8889/node", {
          params: {
            algorithm: "Algorithm1",
            layer: this.chooseStore.selectedLayer,
          },
        });
        response.data.push("All nodes")
        this.nodes = response.data;
      } catch (error) {
        ElMessage.error("获取节点列表失败: " + error.message);
      }
    },
    async fetchConfigNodes() {
      if (!"Algorithm1" || !this.chooseStore.selectedLayer || !this.chooseStore.selectedNode) {
        return;
      }
      try {
        let sendNode = this.chooseStore.selectedNode
        if(this.chooseStore.selectedNode === "All nodes"){
          sendNode = this.nodes[0]
        }
        const response = await axios.get("http://localhost:8889/config/node", {
          params: {
            algorithm: "Algorithm1",
            layer: this.chooseStore.selectedLayer,
            node: sendNode,
          },
        });
        this.configNodes.length = 0;
        const configs = response.data;
        for(const key in configs){
          this.configNodes.push({
            key: key,
            value: configs[key],
            newValue: configs[key], // 初始化新值为当前值
          })
        }
      } catch (error) {
        ElMessage.error("获取节点配置失败: " + error.message);
      }
    },
    async updateConfig(key, newValue, node) {
      try {
        await axios.post("http://localhost:8889/config/node", {
          layer: this.chooseStore.selectedLayer,
          name: node,
          key: key,
          value: newValue,
        }).then((response) => {
          if(response.status !== 200)throw new Error(response.data.error);
          ElMessage.success("节点配置更新成功");
          this.fetchConfigNodes();
        }).catch( error => {
          ElMessage.error("节点配置更新失败: " + error.message);
        });
      } catch (error) {
        ElMessage.error("节点配置更新失败: " + error.message);
      }
    },
  },
  async mounted() {
    await this.fetchAlgorithms();
    await this.fetchLayers();
    if (this.chooseStore.selectedLayer) {
      await this.fetchNodes();
      if (this.chooseStore.selectedNode) {
        await this.fetchConfigNodes();
      }
    }
  },
};
</script>

<style scoped>
.config-node-container {
  min-height: 100vh;
  padding: 20px;
}

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
  text-align: left;
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

.selector-section {
  margin-bottom: 30px;
}

.selector-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.selector-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.selector-item {
  margin-bottom: 20px;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.label-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.selector-input {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.status-empty {
  font-size: 13px;
  color: var(--text-secondary);
  opacity: 0.6;
}

.config-content {
  margin-bottom: 20px;
}

.config-card,
.empty-card {
  border-radius: 16px;
  overflow: hidden;
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

/* 表格样式优化 */
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

/* 选择器样式优化 */
.selector-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.selector-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .config-node-container {
    padding: 15px;
  }
  
  .header-content {
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .modify-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .new-value-input {
    min-width: unset;
  }
  
  .status-info {
    padding: 12px;
  }
  
  .status-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
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
  
  .selector-item {
    margin-bottom: 16px;
  }
}
</style>
