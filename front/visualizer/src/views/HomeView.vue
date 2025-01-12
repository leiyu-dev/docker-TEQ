<template>
  <el-container>
    <el-main>
      <!-- 第一行：状态和运行节点 -->
      <el-row :gutter="20" align="top" style="margin-bottom: 20px;">
        <!-- 状态卡片 -->
        <el-col :span="12" style="height: 250px">
          <el-card style="height: 100%;">
            <template #header>
              <span>Status</span>
            </template>
            <div style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
              <div style="display: flex; align-items: center; font-size: 30px; font-weight: bold; margin-bottom: 15px;">
                <span>RUNNING</span>
                <el-icon :size="50" class="icons" style="color: mediumseagreen; margin-left: 10px;">
                  <DArrowRight />
                </el-icon>
              </div>

              <div>
                <el-button type="success" size="large">Run</el-button>
                <el-button type="danger" size="large">Stop</el-button>
                <el-button type="primary" size="large">Restart</el-button>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 运行节点卡片 -->
        <el-col :span="12" style="height: 250px">
          <el-card style="height: 100%">
            <template #header>
              <span>Running Nodes</span>
            </template>
            <div style="padding: 20px; font-size: 20px;">
              <div style="margin-bottom: 15px;">
                <strong>Algorithms:</strong> {{ runningNodes.algorithms }}
              </div>
              <div style="margin-bottom: 15px;">
                <strong>Layers:</strong> {{ runningNodes.layers }}
              </div>
              <div>
                <strong>Nodes:</strong> {{ runningNodes.nodes }}
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
              <span>Memory Usage</span>
            </template>
            <div>
              32GB / 512GB
            </div>
          </el-card>
        </el-col>

        <!-- CPU 使用 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>CPU Usage</span>
            </template>
            <div>
              33.24% / 112 Cores
            </div>
          </el-card>
        </el-col>

        <!-- 运行时间 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>Up Time</span>
            </template>
            <div>
              12min 59s
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>
<script>
export default {
  data() {
    return {
      runningNodes: {
        algorithms: 12,
        layers: 24,
        nodes: 48,
      },
    };
  },
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

.el-header {
  font-size: 20px;
  line-height: 60px;
}

.el-main {
  padding: 20px;
}

.el-card {
  text-align: center;
}
</style>
