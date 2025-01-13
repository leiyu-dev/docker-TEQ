import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ChartView from "../views/ChartView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/chart',
      name: 'chart',
      component: () => import('../views/ChartView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/config/node',
      name: 'config-node',
        component: () => import('../views/ConfigNodeView.vue'),
    },
    {
      path: '/config/global',
      name: 'config-global',
      component: () => import('../views/ConfigGlobalView.vue'),
    }
  ],
})

export default router
