import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  { path: '/scenic', name: 'ScenicList', component: () => import('@/views/ScenicList.vue') },
  { path: '/scenic/:id', name: 'ScenicDetail', component: () => import('@/views/ScenicDetail.vue') },
  { path: '/hotel', name: 'HotelList', component: () => import('@/views/HotelList.vue') },
  { path: '/hotel/:id', name: 'HotelDetail', component: () => import('@/views/HotelDetail.vue') },
  { path: '/orders', name: 'Orders', component: () => import('@/views/Orders.vue'), meta: { auth: true } },
  { path: '/notes', name: 'Notes', component: () => import('@/views/Notes.vue') },
  { path: '/note/:id', name: 'NoteDetail', component: () => import('@/views/NoteDetail.vue') },
  { path: '/publish', name: 'PublishNote', component: () => import('@/views/PublishNote.vue'), meta: { auth: true } },
  { path: '/user', name: 'UserCenter', component: () => import('@/views/UserCenter.vue'), meta: { auth: true } },
  { path: '/search', name: 'Search', component: () => import('@/views/Search.vue') },
  { path: '/admin', name: 'Admin', component: () => import('@/views/Admin.vue'), meta: { auth: true } },
  // 404 catch-all — 必须放最后
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

// 全局导航守卫：检查需要登录的页面
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  if (to.meta.auth) {
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    // Check JWT expiry client-side to avoid flash-of-content-then-redirect
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      if (payload.exp && payload.exp * 1000 < Date.now()) {
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    } catch { /* token format error — let the API interceptor handle it */ }
  }
  // Redirect loop protection: don't pass redirect param for login page itself
  if (to.path === '/login' && from.path === '/login') {
    next({ path: '/login' })
    return
  }
  next()
})

export default router
