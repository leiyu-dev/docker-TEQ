<template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select
              v-model="chooseStore.selectedAlgorithm"
              placeholder="Choose Algorithm"
              style="width: 100%"
              size="large"
              @change="fetchLayers"
          >
            <el-option
                v-for="algorithm in algorithms"
                :key="algorithm"
                :label="algorithm"
                :value="algorithm"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select
              size="large"
              v-model="chooseStore.selectedLayer"
              placeholder="Choose Layer"
              style="width: 100%;"
              @change="fetchNodes"
          >
            <el-option
                v-for="layer in layers"
                :key="layer"
                :label="layer"
                :value="layer"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select
              size="large"
              v-model="chooseStore.selectedNode"
              placeholder="Choose Node"
              style="width: 100%;"
              @change="fetchConfigNodes"
          >
            <el-option
                v-for="node in nodes"
                :key="node"
                :label="node"
                :value="node"
            ></el-option>
          </el-select>
        </el-col>
      </el-row>
    <span style="display: block; text-align: center; margin-top: 20px;">All the Configurations here will be applied in real time</span>
    <el-divider/>
    <el-table :data="configNodes" style="width: 100%; margin-top: 20px;" v-if="configNodes.length">
      <el-table-column prop="key" label="Properties" width="300px"></el-table-column>
      <el-table-column prop="value" label="Current Value(Node 0)" width="250px" v-if="chooseStore.selectedNode==='All nodes'"></el-table-column>
      <el-table-column prop="value" label="Current Value" width="250px" v-else></el-table-column>
      <el-table-column label="Modify" width="500px">
        <template #default="scope">
          <el-row :gutter="10" align="middle">
            <el-col :span="16">
              <el-input v-model="scope.row.newValue" placeholder="Enter new value" />
            </el-col>
            <el-col :span="8">
              <el-button
                  type="primary"
                  @click="updateConfig(scope.row.key, scope.row.newValue,chooseStore.selectedNode)"
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
    async fetchAlgorithms() {
      // this.layers.length = 0;
      // this.chooseStore.selectedLayer = '';
      // this.nodes.length = 0;
      // this.chooseStore.selectedNode = '';
      // this.configNodes.length = 0;
      try {
        const response = await axios.get("http://localhost:8889/algorithm");
        this.algorithms = response.data;
      } catch (error) {
        ElMessage.error("Error fetching algorithms:", error);
      }
    },
    async fetchLayers() {
      // this.nodes.length = 0;
      // this.configNodes.length = 0;
      // this.chooseStore.selectedNode = '';
      try {
        const response = await axios.get("http://localhost:8889/layer", {
          params: {
            ElMessage: this.chooseStore.selectedAlgorithm,
          },
        });
        this.layers = response.data;
      } catch (error) {
        ElMessage.error("Error fetching layers:", error);
      }
    },
    async fetchNodes() {
      // this.configNodes.length = 0;
      if (!this.chooseStore.selectedAlgorithm || !this.chooseStore.selectedLayer) {
        return;
      }
      try {
        const response = await axios.get("http://localhost:8889/node", {
          params: {
            algorithm: this.chooseStore.selectedAlgorithm,
            layer: this.chooseStore.selectedLayer,
          },
        });
        response.data.push("All nodes")
        this.nodes = response.data;
      } catch (error) {
        ElMessage.error("Error fetching nodes:", error);
      }
    },
    async fetchConfigNodes() {
      if (!this.chooseStore.selectedAlgorithm || !this.chooseStore.selectedLayer || !this.chooseStore.selectedAlgorithm) {
        return;
      }
      try {
        let sendNode = this.chooseStore.selectedNode
        if(this.chooseStore.selectedNode === "All nodes"){
          sendNode = this.nodes[0]
        }
        const response = await axios.get("http://localhost:8889/config/node", {
          params: {
            algorithm: this.chooseStore.selectedAlgorithm,
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
          })
        }
      } catch (error) {
        ElMessage.error("Error fetching config nodes:", error);
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
          ElMessage.success("Config updated successfully");
          this.fetchConfigNodes();
        });
      } catch (error) {
        ElMessage.error("Error updating config node:", error);
      }
    },
  },
  async mounted() {
    await this.fetchAlgorithms();
    await this.fetchLayers();
    await this.fetchNodes();
    await this.fetchConfigNodes();
  },
};
</script>

<style>
.el-container {
  height: 100vh;
}

.el-header {
  padding: 10px;
}
</style>
