<template>
  <el-header
      class="modern-header fade-in"
      style="
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      z-index: 10;
      padding: 0 30px;
      height: 70px;
      box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
      backdrop-filter: blur(10px);
    "
  >
    <!-- 左侧部分 -->
    <div style="display: flex; align-items: center;" class="header-left">
      <div class="logo-container">
        <img src="./components/icons/teq.svg" alt="TEQ Icon" class="logo-icon" />
      </div>
      <div class="brand-info">
        <span class="brand-title">TEQ Dashboard</span>
        <span class="brand-subtitle">Edge Computing Query Processing</span>
      </div>
    </div>

    <!-- 右侧部分 -->
    <div class="header-right">
      <div class="version-badge">
        <span>v0.2.0</span>
      </div>
    </div>
  </el-header>

  <el-container style="padding-top: 70px;">
    <!-- 左侧边栏 -->
    <el-aside
        width="220px"
        class="modern-sidebar slide-in-up"
        style="
        position: fixed;
        top: 70px;
        left: 0;
        bottom: 0;
        background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
        border-right: 1px solid rgba(102, 126, 234, 0.1);
        z-index: 0;
        box-shadow: 4px 0 20px rgba(0, 0, 0, 0.08);
      "
    >
      <el-menu
          :default-active="$route.path"
          class="modern-menu"
          router
      >
        <router-link to="/" style="text-decoration: none;">
          <el-menu-item index="/" class="modern-menu-item">
            <div class="menu-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="menu-icon">
                <path fill="currentColor" d="M512 128 128 447.936V896h255.936V640H640v256h255.936V447.936z" />
              </svg>
            </div>
            <span class="menu-text">Overview</span>
          </el-menu-item>
        </router-link>

        <el-sub-menu index="1" class="modern-submenu">
          <template #title>
            <div class="menu-icon-wrapper">
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 1024 1024"
                  class="menu-icon"
              >
                <path
                    fill="currentColor"
                    d="M764.416 254.72a351.68 351.68 0 0 1 86.336 149.184H960v192.064H850.752a351.68 351.68 0 0 1-86.336 149.312l54.72 94.72-166.272 96-54.592-94.72a352.64 352.64 0 0 1-172.48 0L371.136 936l-166.272-96 54.72-94.72a351.68 351.68 0 0 1-86.336-149.312H64v-192h109.248a351.68 351.68 0 0 1 86.336-149.312L204.8 160l166.208-96h.192l54.656 94.592a352.64 352.64 0 0 1 172.48 0L652.8 64h.128L819.2 160l-54.72 94.72zM704 499.968a192 192 0 1 0-384 0 192 192 0 0 0 384 0"
                />
              </svg>
            </div>
            <span class="menu-text">Config</span>
          </template>
          <router-link to="/config/global" style="text-decoration: none;">
            <el-menu-item index="/config/global" class="modern-submenu-item">
              <div class="submenu-indicator"></div>
              <span class="menu-text">Global</span>
            </el-menu-item>
          </router-link>
          <router-link to="/config/node" style="text-decoration: none;">
            <el-menu-item index="/config/node" class="modern-submenu-item">
              <div class="submenu-indicator"></div>
              <span class="menu-text">Node</span>
            </el-menu-item>
          </router-link>
        </el-sub-menu>

        <router-link to="/chart" style="text-decoration: none;">
          <el-menu-item index="/chart" class="modern-menu-item">
            <div class="menu-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="menu-icon">
                <path
                    fill="currentColor"
                    d="M128 896V128h768v768zm291.712-327.296 128 102.4 180.16-201.792-47.744-42.624-139.84 156.608-128-102.4-180.16 201.792 47.744 42.624 139.84-156.608zM816 352a48 48 0 1 0-96 0 48 48 0 0 0 96 0"
                />
              </svg>
            </div>
            <span class="menu-text">Statistics</span>
          </el-menu-item>
        </router-link>

        <router-link to="/about" style="text-decoration: none;">
          <el-menu-item index="/about" class="modern-menu-item">
            <div class="menu-icon-wrapper">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" class="menu-icon">
                <path
                    fill="currentColor"
                    d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"
                />
              </svg>
            </div>
            <span class="menu-text">About</span>
          </el-menu-item>
        </router-link>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-main class="modern-main" style="margin-left: 240px; min-height: calc(100vh - 70px); padding: 30px;">
      <div class="content-wrapper fade-in">
        <router-view />
      </div>
    </el-main>
  </el-container>
</template>

<script>
export default {
  watch: {
    $route(to) {
      this.activeMenu = to.path;
    },
  },
};
</script>

<style>
/* 头部样式 */
.modern-header {
  position: relative;
  overflow: hidden;
}

.modern-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="rgba(255,255,255,0.05)"><polygon points="0,0 1000,0 1000,100 0,80"/></svg>');
  pointer-events: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-container {
  padding: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.logo-icon {
  width: 45px;
  height: 45px;
  filter: brightness(0) invert(1);
  transition: transform 0.3s ease;
}

.logo-container:hover .logo-icon {
  transform: rotate(360deg) scale(1.1);
}

.brand-info {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 12px;
  opacity: 0.8;
  margin: 0;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
}

.version-badge {
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

/* 侧边栏样式 */
.modern-sidebar {
  position: relative;
}

.modern-sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(to bottom, transparent, rgba(102, 126, 234, 0.2), transparent);
}

.modern-menu {
  border: none !important;
  background: transparent !important;
  padding: 20px 0;
}

.modern-menu-item,
.modern-submenu-item {
  margin: 8px 15px !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  border: none !important;
  background: transparent !important;
  color: var(--text-primary) !important;
  position: relative;
  overflow: hidden;
}

.modern-menu-item::before,
.modern-submenu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.modern-menu-item:hover::before,
.modern-submenu-item:hover::before {
  opacity: 0.1;
}

.modern-menu-item.is-active,
.modern-submenu-item.is-active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1)) !important;
  color: var(--primary-color) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2) !important;
}

.modern-menu-item.is-active::before,
.modern-submenu-item.is-active::before {
  opacity: 0.15;
}

.menu-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  margin-right: 12px;
  border-radius: 8px;
  background: rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.modern-menu-item:hover .menu-icon-wrapper,
.modern-submenu-item:hover .menu-icon-wrapper {
  background: rgba(102, 126, 234, 0.2);
  transform: scale(1.1);
}

.menu-icon {
  width: 18px;
  height: 18px;
  color: var(--primary-color);
}

.menu-text {
  font-size: 15px;
  font-weight: 500;
  letter-spacing: -0.2px;
}

.modern-submenu {
  margin: 8px 15px !important;
}

.modern-submenu .el-sub-menu__title {
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  color: var(--text-primary) !important;
  font-weight: 500;
  padding: 0 20px !important;
  height: 48px !important;
}

.modern-submenu .el-sub-menu__title:hover {
  background: rgba(102, 126, 234, 0.05) !important;
}

.submenu-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--primary-color);
  margin-right: 12px;
  opacity: 0.6;
}

.modern-submenu-item.is-active .submenu-indicator {
  opacity: 1;
  box-shadow: 0 0 8px rgba(102, 126, 234, 0.6);
}

/* 主内容区域样式 */
.modern-main {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  min-height: calc(100vh - 70px);
  position: relative;
}

.modern-main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(118, 75, 162, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(240, 147, 251, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

.content-wrapper {
  position: relative;
  z-index: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modern-sidebar {
    width: 200px !important;
  }
  
  .modern-main {
    margin-left: 220px !important;
    padding: 20px !important;
  }
  
  .brand-title {
    font-size: 20px !important;
  }
  
  .brand-subtitle {
    display: none;
  }
}

@media (max-width: 480px) {
  .modern-header {
    padding: 0 15px !important;
  }
  
  .modern-sidebar {
    width: 180px !important;
  }
  
  .modern-main {
    margin-left: 200px !important;
    padding: 15px !important;
  }
}
</style>
