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
      path: '/data-analysis',
      name: 'data-analysis',
      component: () => import('../views/DataAnalysisView.vue'),
      meta: { requiresAuth: true },
    },
    
  ],
})

router.beforeEach((to, from) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.LogCondition) {
    authStore.redirectPath = to.fullPath
    authStore.showLogin = true
    if (to.path !== '/home') {
      return { name: 'home' }
    }
  }
})
export default router
