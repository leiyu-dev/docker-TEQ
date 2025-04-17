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

    // 在组件挂载时获取数据
    onMounted(() => {
      fetchOptions();
      if (chooseStore.selectedConfig) {
        fetchConfigDetails(chooseStore.selectedConfig);
      }
    });

    const saveDefault = () => {
      ElMessage.success("Save Successfully!");
    }

    return {
      chooseStore,
      options,
      configDetails,
      saveDefault,
      fetchConfigDetails,
      updateConfigParameter,
    };
  },
};
</script>

<template>
  <main></main>
  <div style="display: flex; align-items: center;">
    <el-select
        v-model="chooseStore.selectedConfig"
        placeholder="Select a config class"
        size="large"
        style="width: 340px"
        @change="fetchConfigDetails"
    >
      <el-option
          v-for="item in options"
          :key="item.value"
          :label="item.label"
          :value="item.value"
      ></el-option>
    </el-select>
    <el-button type="primary" @click="saveDefault" style="margin-left: 20px">
      Save as default params
    </el-button>
    <div style="margin-left: 20px">
      Global configurations for the application. Changes during runtime may cause unexpected behavior.
    </div>
  </div>
  <el-divider />

  <!-- 参数详情表格 -->
  <el-table :data="configDetails" v-if="configDetails.length!==0" style="width: 100%; margin-top: 20px;">
    <el-table-column prop="key" label="Properties" width="300px"></el-table-column>
    <el-table-column prop="value" label="Current Value" width="400px"></el-table-column>
    <el-table-column label="Modify" width="500px">
      <template #default="scope">
        <el-row :gutter="10" align="middle">
          <el-col :span="16">
            <el-input v-model="scope.row.newValue" placeholder="Enter new value" />
          </el-col>
          <el-col :span="8">
            <el-button
                type="primary"
                @click="updateConfigParameter(scope.row.key, scope.row.newValue, chooseStore.selectedConfig)"
            >
              Update
            </el-button>
          </el-col>
        </el-row>
      </template>
    </el-table-column>
  </el-table>
  <el-empty v-else description=""></el-empty>
</template>
