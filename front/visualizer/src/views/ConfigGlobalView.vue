<script>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useChooseStore } from "@/stores/choose.js";

export default {
  setup() {
    const chooseStore = useChooseStore();
    const options = ref([]);
    const configDetails = reactive([]); // 存储当前选中配置的详细参数

    // 获取下拉框数据
    const fetchOptions = () => {
      fetch("http://localhost:8889/config/name")
          .then((response) => {
            if (response.status !== 200) {
              throw Error("Failed to fetch configurations");
            }
            return response.json();
          })
          .then((data) => {
            console.log(data);
            options.value = data.map((name) => ({ value: name, label: name }));
          })
          .catch((error) => {
            ElMessage.error(error.message);
          });
    };

    // 获取选中选项的详细配置
    const fetchConfigDetails = (selected) => {
      if (!selected) return;

      fetch(`http://localhost:8889/config/detail?name=${selected}`)
          .then((response) => {
            if (response.status !== 200) {
              throw Error(response.statusText);
            }
            return response.json();
          })
          .then((data) => {
            console.log(data);
            configDetails.length = 0; // 清空之前的数据
            for (const key in data) {
              configDetails.push({
                key: key,
                value: data[key],
                newValue: data[key], // 初始化新值为当前值
              });
            }
          })
          .catch((error) => {
            ElMessage.error(error.message);
          });
    };

    // 提交修改参数请求
    const updateConfigParameter = (key, newValue, selectedName) => {
      fetch("http://localhost:8889/config", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ name: selectedName, key, value: newValue }), // 包含选项名称
      })
          .then((response) => {
            if (response.status !== 200) {
              throw Error("Failed to update configuration");
            }
            fetchConfigDetails(selectedName);
            ElMessage.success("Configuration updated successfully!");
          })
          .catch((error) => {
            ElMessage.error(error.message);
          });
    };

    // 获取值类型对应的标签类型
    const getValueType = (value) => {
      if (typeof value === 'boolean') return 'success';
      if (typeof value === 'number') return 'warning';
      if (typeof value === 'string') {
        if (value.toLowerCase() === 'true' || value.toLowerCase() === 'false') return 'success';
        if (!isNaN(value)) return 'warning';
      }
      return 'info';
    };

    // 在组件挂载时获取数据
    onMounted(() => {
      fetchOptions();
      if (chooseStore.selectedConfig) {
        fetchConfigDetails(chooseStore.selectedConfig);
      }
    });

    const saveDefault = () => {
      ElMessage.success("Configuration saved as default parameters!");
    }

    return {
      chooseStore,
      options,
      configDetails,
      saveDefault,
      fetchConfigDetails,
      updateConfigParameter,
      getValueType,
    };
  },
};
</script>

<template>
  <div class="config-global-container fade-in">
    <!-- 页面标题和操作区域 -->
    <div class="page-header slide-in-up">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title gradient-text">Global Configuration Management</h1>
          <p class="page-subtitle">Manage system global parameter configurations. Runtime modifications may cause unexpected behavior</p>
        </div>
        <div class="header-actions">
          <el-select
              v-model="chooseStore.selectedConfig"
              placeholder="Select Configuration Class"
              size="large"
              class="config-selector"
              @change="fetchConfigDetails"
          >
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            ></el-option>
          </el-select>
          <el-button 
            type="primary" 
            size="large" 
            @click="saveDefault" 
            class="save-btn glow-on-hover"
            :disabled="!chooseStore.selectedConfig"
          >
            <el-icon><DocumentAdd /></el-icon>
            Save as Default Parameters
          </el-button>
        </div>
      </div>
    </div>

    <!-- 配置详情卡片 -->
    <div class="config-content slide-in-up" style="animation-delay: 0.2s;">
      <el-card class="config-card hover-lift" v-if="configDetails.length !== 0">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="config-title">{{ chooseStore.selectedConfig }}</span>
              <span class="config-count">{{ configDetails.length }} parameters</span>
            </div>
            <div class="header-right">
              <el-tag type="info" size="small">Global Config</el-tag>
            </div>
          </div>
        </template>

        <!-- 参数配置表格 -->
        <el-table 
          :data="configDetails" 
          class="config-table"
          :header-cell-style="{ backgroundColor: '#f8fafc', color: '#2d3748' }"
        >
          <el-table-column prop="key" label="Parameter Name" width="300px">
            <template #default="scope">
              <div class="param-name">
                <el-icon class="param-icon"><Setting /></el-icon>
                <span>{{ scope.row.key }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="value" label="Current Value" width="300px">
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
          
          <el-table-column label="Modify Configuration" min-width="500px">
            <template #default="scope">
              <div class="modify-section">
                <el-input 
                  v-model="scope.row.newValue" 
                  placeholder="Enter new configuration value"
                  class="new-value-input"
                  clearable
                />
                <el-button
                    type="primary"
                    @click="updateConfigParameter(scope.row.key, scope.row.newValue, chooseStore.selectedConfig)"
                    class="update-btn"
                    :disabled="!scope.row.newValue || scope.row.newValue === scope.row.value"
                >
                  <el-icon><Check /></el-icon>
                  Update
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 空状态 -->
      <el-card class="empty-card hover-lift" v-else>
        <el-empty 
          description="Please select a configuration class to view parameters"
          :image-size="120"
        >
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><Setting /></el-icon>
            </div>
          </template>
          <template #description>
            <span class="empty-text">Please select configuration class</span>
          </template>
        </el-empty>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.config-global-container {
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

.config-content {
  margin-bottom: 20px;
}

.config-card,
.empty-card {
  border-radius: 16px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .config-global-container {
    padding: 15px;
  }
  
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
  
  .modify-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .new-value-input {
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
