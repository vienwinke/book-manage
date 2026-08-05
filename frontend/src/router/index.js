import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页看板', role: 'admin' }
      },
      {
        path: 'book',
        name: 'Book',
        component: () => import('../views/Book.vue'),
        meta: { title: '图书管理', role: 'admin' }
      },
      {
        path: 'library',
        name: 'Library',
        component: () => import('../views/Library.vue'),
        meta: { title: '图书浏览', role: 'student' }
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('../views/Borrow.vue'),
        meta: { title: '借阅管理', role: 'admin' }
      },
      {
        path: 'my-borrow',
        name: 'MyBorrow',
        component: () => import('../views/MyBorrow.vue'),
        meta: { title: '我的借阅', role: 'student' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '用户管理', role: 'admin' }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('../views/Log.vue'),
        meta: { title: '日志管理', role: 'admin' }
      }
    ]
  },
  {
    // 兼容旧版跳转：/home 重定向到看板
    path: '/home',
    redirect: '/dashboard'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const roleOf = user => (user && user.userType === 1 ? 'admin' : 'student')

// 路由守卫：登录校验 + 角色校验
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.path === '/login' || to.path === '/register') {
    return token ? next(roleOf(user) === 'admin' ? '/dashboard' : '/library') : next()
  }
  if (!token) {
    return next('/login')
  }
  // 默认落地页按角色
  if (to.path === '/') {
    return next(roleOf(user) === 'admin' ? '/dashboard' : '/library')
  }
  // 角色拦截
  const need = to.meta.role
  if (need && roleOf(user) !== need) {
    return next(roleOf(user) === 'admin' ? '/dashboard' : '/library')
  }
  next()
})

export default router
