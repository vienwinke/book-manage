<template>
  <div>
    <el-row :gutter="16">
      <el-col v-for="c in cards" :key="c.label" :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: c.bg, color: c.color }">
            <el-icon :size="26"><component :is="c.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近操作日志</span>
          <el-button link type="primary" @click="$router.push('/log')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentLogs" size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="description" label="操作内容" min-width="220" />
        <el-table-column prop="ip" label="IP" width="130" />
        <el-table-column prop="operateTime" label="操作时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Notebook, Tickets, Document, User, Memo, CircleCheck } from '@element-plus/icons-vue'
import { pageBooks } from '../api/book'
import { pageBorrows } from '../api/borrow'
import { pageUsers } from '../api/user'
import { pageLogs } from '../api/log'

const bookTotal = ref(0)
const availableTotal = ref(0)
const pendingCount = ref(0)
const borrowedCount = ref(0)
const userCount = ref(0)
const logCount = ref(0)
const recentLogs = ref([])

const cards = computed(() => [
  { label: '图书总数', value: bookTotal.value, icon: Notebook, bg: '#ecf5ff', color: '#409EFF' },
  { label: '可借总数', value: availableTotal.value, icon: CircleCheck, bg: '#f0f9eb', color: '#67c23a' },
  { label: '待审核', value: pendingCount.value, icon: Tickets, bg: '#fdf6ec', color: '#e6a23c' },
  { label: '借出中', value: borrowedCount.value, icon: Document, bg: '#f4f4f5', color: '#909399' },
  { label: '用户数', value: userCount.value, icon: User, bg: '#fef0f0', color: '#f56c6c' },
  { label: '日志数', value: logCount.value, icon: Memo, bg: '#f9f0ff', color: '#9c27b0' }
])

onMounted(async () => {
  const [books, users, logs, pendingBorrows, borrowedBorrows] = await Promise.all([
    pageBooks({ pageNum: 1, pageSize: 100 }),
    pageUsers({ pageNum: 1, pageSize: 1 }),
    pageLogs({ pageNum: 1, pageSize: 5 }),
    pageBorrows({ pageNum: 1, pageSize: 1, status: 0 }),
    pageBorrows({ pageNum: 1, pageSize: 1, status: 1 })
  ])
  bookTotal.value = books.total
  availableTotal.value = books.records.reduce((s, b) => s + (b.availableNum || 0), 0)
  userCount.value = users.total
  logCount.value = logs.total
  recentLogs.value = logs.records
  pendingCount.value = pendingBorrows.total
  borrowedCount.value = borrowedBorrows.total
})
</script>

<style scoped>
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 14px; padding: 18px; }
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-num { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; }
.recent-card { margin-top: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
