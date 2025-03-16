import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path:'/usage',
      name: 'usage',
      component: () => import('../views/UsageView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path:'/map',
      name:'detection-map',
      component: () => import('../views/DetectionMapView.vue'),
      meta: { requiresAuth: true },

    },
    {
      path:'/realtime',
      name:'realtime',
      component: () => import('../views/RealTimeView.vue')
    },
    {
      path:'/file-upload',
      name:'file-upload',
      component: () => import('../views/FileUploadView.vue'),
      meta: { requiresAuth: true },

    },
    {
      path: '/allData',
      name: 'allData',
      component: () => import('../views/AllDataListView.vue'),
      meta: { requiresAuth: true },
    },

  ],
})

router.beforeEach((to, from) => {
  const authStore = useAuthStore()
  authStore.lastPath = from.fullPath
  if (to.meta.requiresAuth && !authStore.LogCondition) {
    authStore.redirectPath = to.fullPath
    authStore.LoginVisible = true
    if (to.path !== '/home') {
      return { name: 'home' }
    }
  }
})
export default router
