<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';

const value = ref('');
const options = ref([]);
const configDetails = reactive([]); // 存储当前选中配置的详细参数

// 获取下拉框数据
fetch('http://localhost:8889/config/name')
    .then((response) => {
      if (response.status !== 200) {
        throw Error('Failed to fetch configurations');
      }
      return response.json();
    })
    .then((data) => {
      console.log(data);
      for (const name of data) {
        options.value.push({ value: name, label: name });
      }
    })
    .catch((error) => {
      ElMessage.error(error.message);
    });

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
        for(const key in data) {
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
  fetch('http://localhost:8889/config', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ name: selectedName, key, value: newValue }), // 包含选项名称
  })
      .then((response) => {
        if (response.status !== 200) {
          throw Error('Failed to update configuration');
        }
        fetchConfigDetails(value.value);
        ElMessage.success('Configuration updated successfully!');
      })
      .catch((error) => {
        ElMessage.error(error.message);
      });
};

</script>

<template>
  <main></main>
  <div style="display: flex; align-items: center;">
    <el-select
        v-model="value"
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
    <el-button
        type="primary"
        @click=""
        style="margin-left: 20px"
    >Save as default params</el-button>
    <div style="margin-left: 20px">
      Global configurations for the application. Changes during runtime may cause unexpected behavior.
    </div>

  </div>
  <el-divider />

  <!-- 参数详情表格 -->
  <el-table :data="configDetails" style="width: 100%; margin-top: 20px;">
    <el-table-column prop="key" label="Key" width="300px"></el-table-column>
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
                @click="updateConfigParameter(scope.row.key, scope.row.newValue,value)"
            >
              Update
            </el-button>
          </el-col>
        </el-row>
      </template>
    </el-table-column>
  </el-table>
</template>
