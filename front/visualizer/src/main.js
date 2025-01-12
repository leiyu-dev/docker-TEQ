import './assets/base.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useChartStore } from './stores/chart';
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
chartStore.fetchChartConfig();
chartStore.startFetching();

app.mount('#app')

