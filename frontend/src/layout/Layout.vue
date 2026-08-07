<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo-area">
        <el-icon :size="22" color="#409EFF"><Reading /></el-icon>
        <span>图书管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        class="menu"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">{{ route.meta.title }}</div>
        <div class="user-info">
          <el-avatar :size="32" style="background: #409EFF">
            {{ userStore.user?.realName?.[0] || '用' }}
          </el-avatar>
          <span class="username">{{ userStore.user?.realName || userStore.user?.username }}</span>
          <el-tag size="small" :type="isAdmin ? 'danger' : 'success'">
            {{ isAdmin ? '管理员' : '学生' }}
          </el-tag>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading, DataLine, Notebook, Collection,
  Document, Tickets, User, Memo
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userType = computed(() => {
  const type = userStore.user?.userType
  return type === 1 ? 1 : 0
})

const isAdmin = computed(() => userType.value === 1)

watch(() => route.path, () => {
  userStore.refreshUser()
})

const allMenus = [
  { path: '/dashboard', title: '首页看板', icon: DataLine, role: 'admin' },
  { path: '/book', title: '图书管理', icon: Notebook, role: 'admin' },
  { path: '/library', title: '图书浏览', icon: Collection, role: 'student' },
  { path: '/order', title: '订单管理', icon: Tickets, role: 'admin' },
  { path: '/my-order', title: '我的订单', icon: Document, role: 'student' },
  { path: '/user', title: '用户管理', icon: User, role: 'admin' },
  { path: '/log', title: '日志管理', icon: Memo, role: 'admin' }
]

const menus = computed(() => allMenus.filter(m => m.role === (isAdmin.value ? 'admin' : 'student')))

const handleLogout = async () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100%; }
.aside {
  background: #1f2d3d;
  display: flex;
  flex-direction: column;
}
.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.menu { border-right: none; flex: 1; }
.header {
  background: #fff;
  box-shadow: 0 1px 6px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 1;
}
.breadcrumb { font-size: 15px; font-weight: 600; color: #303133; }
.user-info { display: flex; align-items: center; gap: 10px; }
.username { font-size: 14px; color: #303133; }
.main { background: #f0f2f5; }
</style>
