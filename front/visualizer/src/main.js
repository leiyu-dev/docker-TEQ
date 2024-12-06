import './assets/base.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import VueECharts from 'vue-echarts';

const app = createApp(App)
app.component('vue-echarts', VueECharts);
app.use(ElementPlus)
app.use(router)

app.mount('#app')
