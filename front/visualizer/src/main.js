import './assets/base.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useChartStore } from './stores/chart';
import { useStatusStore } from './stores/status';
import { useLogsStore } from './stores/logs';
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia();
app.use(ElementPlus)
app.use(router)
app.use(pinia);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
const chartStore = useChartStore();
const statusStore = useStatusStore();
const logsStore = useLogsStore();
chartStore.fetchChartConfig();
chartStore.startFetching();
statusStore.startFetching();
logsStore.pollLogs();
app.mount('#app')

