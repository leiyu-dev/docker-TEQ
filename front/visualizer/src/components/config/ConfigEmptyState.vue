<!--
  ConfigEmptyState Component
  Displays empty state when no configuration is selected
-->
<template>
  <el-card class="empty-card hover-lift">
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
        <span class="empty-text">Please select a configuration class</span>
      </template>
    </el-empty>
  </el-card>
</template>

<script>
export default {
  name: 'ConfigEmptyState'
};
</script>

<style scoped>
.empty-card {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  overflow: hidden;
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
</style> 