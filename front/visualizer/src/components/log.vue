<template>
  <div class="log-display">
    <pre class="log-content">{{ logs }}</pre>
  </div>
</template>

<script>
export default {
  name: "LogDisplay",
  props: {
    logs: {
      type: String,
      default: "", // 默认日志内容为空
    },
  },
};
</script>

<style scoped>
.log-display {
  background-color: #1e1e1e; /* 代码框背景色 */
  color: #dcdcdc; /* 字体颜色 */
  font-family: "Courier New", Courier, monospace; /* 等宽字体 */
  padding: 10px;
  border-radius: 5px; /* 圆角 */
  border: 1px solid #444; /* 边框 */
  overflow-y: auto; /* 滚动条 */
  height: 300px; /* 固定高度 */
  white-space: pre-wrap; /* 保留换行符 */
  word-break: break-word; /* 长字符串换行 */
}
.log-content {
  margin: 0;
  line-height: 1.5; /* 设置行高 */
}
</style>
