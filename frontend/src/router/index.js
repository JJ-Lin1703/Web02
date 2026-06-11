import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', component: () => import('../views/Home.vue') },
      { path: 'record', component: () => import('../views/HealthRecord.vue') },
      { path: 'ai-plan', component: () => import('../views/AiPlan.vue') },
      { path: 'history', component: () => import('../views/History.vue') },
      { path: 'chart', component: () => import('../views/Chart.vue') },
      { path: 'doc-qa', component: () => import('../views/DocQA.vue') },
      { 
        path: 'admin', 
        component: () => import('../views/AdminHome.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('../views/404.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }
  
  if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    next('/')
    return
  }
  
  if (to.path === '/login' && userStore.token) {
    next('/')
    return
  }
  
  next()
})

export default router