<template>
  <el-card shadow="never">
    <el-tabs v-model="statusTab" @tab-change="onTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待确认" name="0" />
      <el-tab-pane label="已成交" name="1" />
      <el-tab-pane label="已取消" name="2" />
      <el-tab-pane label="已拒绝" name="3" />
    </el-tabs>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookName" label="图书" min-width="150" show-overflow-tooltip />
      <el-table-column label="卖家" width="110">
        <template #default="{ row }">{{ row.sellerName || '—' }}</template>
      </el-table-column>
      <el-table-column label="成交价" width="90" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600">¥{{ row.orderPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="下单时间" width="165" />
      <el-table-column prop="doneTime" label="完成时间" width="165" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" width="130" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="danger" @click="handleCancel(row)">取消订单</el-button>
          <span v-else style="color: #c0c4cc">—</span>
        </template>
      </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageOrders, cancelOrder } from '../api/order'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: null, buyerId: userStore.user?.id })
const statusTab = ref('all')

const onTabChange = () => {
  query.status = statusTab.value === 'all' ? null : Number(statusTab.value)
  loadData(1)
}

const statusMap = { 0: '待确认', 1: '已成交', 2: '已取消', 3: '已拒绝' }
const statusType = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageOrders(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleCancel = row => {
  ElMessageBox.confirm(`确定取消《${row.bookName}》的订单吗？`, '取消确认', { type: 'warning' })
    .then(async () => {
      try {
        await cancelOrder(row.id)
        ElMessage.success('已取消订单')
        loadData(1)
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    })
    .catch(() => {})
}

onMounted(() => loadData(1))
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>