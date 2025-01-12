<script setup>
</script>


<template>
  <el-container style="height: 100vh; border: 1px solid #dcdfe6;">
    <!-- 顶部导航栏 -->
    <el-header
        style="
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    background-color: #4da6ec;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between; /* 调整子项的水平对齐方式 */
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 10;
  "
    >
      <span style="font-size: 18px; font-weight: bold;">Teq Dashboard</span>
      <div style="font-size: 14px;">version: 0.1</div>
    </el-header>

    <el-container style="margin-top: 60px;">
      <!-- 左侧边栏 -->
      <el-aside width="180px" style="position: fixed; top: 60px; left: 0; bottom: 0; background-color: rgb(121.3, 187.1, 255); color: #fff; z-index: 9;">
        <el-menu
            :default-active="'/'"
            class="el-menu-vertical-demo"
            background-color="rgb(121.3, 187.1, 255)"
            text-color="#fff"
            active-text-color="#fff"
        >
          <router-link to="/" style="text-decoration: none; color: inherit;">
            <el-menu-item index="/" class="menu-item">
              <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="icon"><path fill="currentColor" d="M512 128 128 447.936V896h255.936V640H640v256h255.936V447.936z"></path></svg>
              <span class="sidebar">Overview</span>
            </el-menu-item>
          </router-link>


          <router-link to="/config" style="text-decoration: none; color: inherit;">
          <el-menu-item index="/config" class="menu-item">
            <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="icon"><path fill="currentColor" d="M764.416 254.72a351.68 351.68 0 0 1 86.336 149.184H960v192.064H850.752a351.68 351.68 0 0 1-86.336 149.312l54.72 94.72-166.272 96-54.592-94.72a352.64 352.64 0 0 1-172.48 0L371.136 936l-166.272-96 54.72-94.72a351.68 351.68 0 0 1-86.336-149.312H64v-192h109.248a351.68 351.68 0 0 1 86.336-149.312L204.8 160l166.208-96h.192l54.656 94.592a352.64 352.64 0 0 1 172.48 0L652.8 64h.128L819.2 160l-54.72 94.72zM704 499.968a192 192 0 1 0-384 0 192 192 0 0 0 384 0"></path></svg>
              <span class="sidebar">Configuration</span>
          </el-menu-item>
          </router-link>

          <router-link to="/chart" style="text-decoration: none; color: inherit;">
          <el-menu-item index="/chart" class="menu-item">
            <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="icon"><path fill="currentColor" d="M128 896V128h768v768zm291.712-327.296 128 102.4 180.16-201.792-47.744-42.624-139.84 156.608-128-102.4-180.16 201.792 47.744 42.624 139.84-156.608zM816 352a48 48 0 1 0-96 0 48 48 0 0 0 96 0"></path></svg>
            <span class="sidebar">Statistics</span>
          </el-menu-item>
          </router-link>

          <router-link to="/about" style="text-decoration: none; color: inherit;">
          <el-menu-item index="/about" class="menu-item">
            <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"class="icon"><path fill="currentColor" d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"></path></svg>
            <span class="sidebar">About</span>
          </el-menu-item>
          </router-link>

        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main style="margin-left: 220px;">
          <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>


<script>
export default {
  mounted() {
    // 页面加载时强制跳转到 `/`
    if (this.$route.path !== '/') {
      this.$router.push('/'); // 强制跳转到 `/`
    }
  },
};
</script>


<style>
 .sidebar {
   font-size: 17px;
   font-weight: 500;
 }
 .el-menu-item.is-active{
    background-color: #4da6ec;
    color: #fff;
 }
 .menu-item {
   display:flex;
   align-items: flex-start;
   gap: 8px;
 }
 .icon{
   width: 20px;
    height: 20px;
 }
</style>