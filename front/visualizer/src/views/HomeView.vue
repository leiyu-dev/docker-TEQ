<template>
      <!-- 第一行：状态和运行节点 -->
      <el-row :gutter="20" align="top" style="margin-bottom: 20px;">
        <!-- 状态卡片 -->
        <el-col :span="12" style="height: 250px">
          <el-card style="height: 100%;">
            <template #header>
              <span class="headers">Status</span>
            </template>
            <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">

              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;" v-if="statusStore.status==='RUNNING'">
                <span>RUNNING</span>
                <el-icon :size="50" class="icons" style="color: mediumseagreen; margin-left: 10px;">
                  <DArrowRight />
                </el-icon>
              </div>

              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;" v-if="statusStore.status==='RESTARTING'">
                <span>RESTARTING</span>
                <el-icon :size="50" class="icons" style="color: darkorange; margin-left: 10px;">
                  <Refresh />
                </el-icon>
              </div>

              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;" v-if="statusStore.status==='STOPPING'">
                <span>STOPPING</span>
                <el-icon :size="50" class="icons" style="color: darkorange; margin-left: 10px;">
                  <Refresh />
                </el-icon>
              </div>

              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;" v-if="statusStore.status==='STOPPED'">
                <span>STOPPED</span>
                <el-icon :size="50" class="icons" style="color: rgba(255,0,0,0.71); margin-left: 10px;">
                  <CircleClose />
                </el-icon>
              </div>

              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;" v-if="statusStore.status==='DISCONNECTED'">
                <span>DISCONNECTED</span>
                <el-icon :size="50" class="icons" style="color: grey; margin-left: 10px;">
                  <Loading />
                </el-icon>
              </div>

              <div>
                <el-button type="success" size="large" @click="start()">Start</el-button>
                <el-button type="danger" size="large" @click="stop()">Stop</el-button>
                <el-button type="primary" size="large" @click="restart()">Restart</el-button>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 运行节点卡片 -->
        <el-col :span="12" style="height: 250px">
          <el-card style="height: 100%">
            <template #header>
              <span class="headers">Running Nodes</span>
            </template>
            <div style="padding: 20px; font-size: 20px;">
              <div style="margin-bottom: 15px;">
                <strong>Algorithms:</strong> {{ statusStore.algorithms }}
              </div>
              <div style="margin-bottom: 15px;">
                <strong>Layers:</strong> {{ statusStore.layers }}
              </div>
              <div>
                <strong>Nodes:</strong> {{ statusStore.nodes }}
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>


      <!-- 第二行：内存、CPU 和运行时间 -->
      <el-row :gutter="20">
        <!-- 内存使用 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span class="headers">Memory Usage</span>
            </template>
            <div>
              {{ statusStore.memoryUsage }}
            </div>
          </el-card>
        </el-col>

        <!-- CPU 使用 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span class="headers">CPU Usage</span>
            </template>
            <div>
              {{ statusStore.cpuUsage }}
            </div>
          </el-card>
        </el-col>

        <!-- 运行时间 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span class="headers">Up Time</span>
            </template>
            <div>
              {{ statusStore.uptime }}
            </div>
          </el-card>
        </el-col>
      </el-row>
  </template>
<script>
import {ElMessage} from "element-plus";
import {useStatusStore} from "@/stores/status.js";
export default {
  data() {
    return {
      statusStore : useStatusStore(),
    };
  },
  mounted() {

  },
  methods:{
    start(){
      if(this.statusStore.status === 'RUNNING'){
        ElMessage.warning('The simulator is already running');
        return;
      }
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('The simulator is already restarting');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('The simulator is disconnected');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('The simulator is stopping');
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
          ElMessage.success('The simulator is started');
          this.statusStore.status = 'RUNNING';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('There has been a problem with your fetch operation: ' + error.message);
      });
    },
    stop(){
      if(this.statusStore.status === 'STOPPED'){
        ElMessage.warning('The simulator is already stopped');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('The simulator is disconnected');
        return;
      }
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('The simulator is restarting');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('The simulator is stopping');
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
          ElMessage.success('The simulator has been stopped');
          this.statusStore.status = 'STOPPED';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('There has been a problem with your fetch operation: ' + error.message);
      });
    },
    restart(){
      if(this.statusStore.status === 'RESTARTING'){
        ElMessage.warning('The simulator is already restarting');
        return;
      }
      if(this.statusStore.status === 'DISCONNECTED'){
        ElMessage.warning('The simulator is disconnected');
        return;
      }
      if(this.statusStore.status === 'STOPPED'){
        ElMessage.warning('The simulator is stopped');
        return;
      }
      if(this.statusStore.status === 'STOPPING'){
        ElMessage.warning('The simulator is stopping');
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
          ElMessage.success('The simulator is restarting');
          this.statusStore.status = 'RESTARTING';
        }else{
          ElMessage.error(data.message);
        }
      }).catch(error => {
        ElMessage.error('There has been a problem with your fetch operation: ' + error.message);
      });
    }
  }
};
</script>
<style>
body {
  font-family: Arial, sans-serif;
  margin: 0;
  padding: 0;
}

.icons{
  margin-left: 10px;
  font-size: 100px;
}

.headers {
  font-size: 25px;
  font-weight: 100;
  padding: 0;
  margin: 0;
}

.el-main {
  padding: 20px;
}

.el-card {
  text-align: center;
}
</style>
