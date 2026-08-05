<template>
  <el-card shadow="never">
    <el-tabs v-model="statusTab" @tab-change="onTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待审核" name="0" />
      <el-tab-pane label="借出中" name="1" />
      <el-tab-pane label="已归还" name="2" />
      <el-tab-pane label="已拒绝" name="3" />
    </el-tabs>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图书" min-width="150">
        <template #default="{ row }">{{ nameOf(books, row.bookId) }}</template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="165" />
      <el-table-column prop="expectReturnTime" label="预计归还" width="110" />
      <el-table-column prop="actualReturnTime" label="实际归还" width="165" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="管理员备注" width="130" show-overflow-tooltip />
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="loadData"
    />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageBorrows } from '../api/borrow'
import { pageBooks } from '../api/book'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const books = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, status: null, userId: userStore.user?.id })
const statusTab = ref('all')

const onTabChange = () => {
  query.status = statusTab.value === 'all' ? null : Number(statusTab.value)
  loadData(1)
}

const statusMap = { 0: '待审核', 1: '借出中', 2: '已归还', 3: '已拒绝' }
const statusType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }

const nameOf = (arr, id) => arr.find(i => i.id === id)?.bookName || id

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageBorrows(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const data = await pageBooks({ pageNum: 1, pageSize: 100 })
  books.value = data.records
  loadData(1)
})
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
