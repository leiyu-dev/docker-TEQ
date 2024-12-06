<template>
  <el-container style="height: 100vh; border: 1px solid #dcdfe6;">
    <!-- 顶部导航栏 -->
    <el-header style="background-color: #4da6ec; color: #fff; display: flex; align-items: center; position: fixed; top: 0; left: 0; width: 100%; z-index: 10;">
      <el-icon style="margin-right: 10px;">
        <i class="el-icon-menu"></i>
      </el-icon>
      <span style="font-size: 18px; font-weight: bold;">Teq Dashboard</span>
    </el-header>

    <el-container style="margin-top: 60px;">
      <!-- 左侧边栏 -->
      <el-aside width="220px" style="position: fixed; top: 60px; left: 0; bottom: 0; background-color: rgb(121.3, 187.1, 255); color: #fff; z-index: 9;">
        <el-menu
            :default-active="'1'"
            class="el-menu-vertical-demo"
            background-color="rgb(121.3, 187.1, 255)"
            text-color="#fff"
            active-text-color="#fff"
        >
          <el-menu-item index="1">
            <i class="el-icon-s-home"></i>
            <span slot="title">Overview</span>
          </el-menu-item>

          <el-sub-menu index="2">
            <template #title>
              <i class="el-icon-s-order"></i>
              <span>Jobs</span>
            </template>
            <el-menu-item index="2-1">Running Jobs</el-menu-item>
            <el-menu-item index="2-2">Completed Jobs</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="3">
            <i class="el-icon-s-custom"></i>
            <span slot="title">Task Managers</span>
          </el-menu-item>

          <el-menu-item index="4">
            <i class="el-icon-upload2"></i>
            <span slot="title">Submit New Job</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main style="margin-left: 220px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
