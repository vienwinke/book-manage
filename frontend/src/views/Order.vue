<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadData(1)">
        <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
      </el-select>
      <el-select v-model="query.buyerId" placeholder="买家" clearable filterable style="width: 160px" @change="loadData(1)">
        <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookName" label="图书" min-width="150" show-overflow-tooltip />
      <el-table-column label="买家" width="110">
        <template #default="{ row }">{{ row.buyerName || userName(row.buyerId) }}</template>
      </el-table-column>
      <el-table-column label="卖家" width="110">
        <template #default="{ row }">{{ row.sellerName || userName(row.sellerId) }}</template>
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
      <el-table-column prop="remark" label="备注" width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="handleConfirm(row)">成交</el-button>
            <el-button link type="danger" @click="openReject(row)">拒绝</el-button>
          </template>
          <el-button v-else link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 拒绝弹窗（填备注） -->
    <el-dialog v-model="rejectVisible" title="拒绝订单" width="400px">
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请输入拒绝原因（选填）" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageOrders, confirmOrder, rejectOrder, deleteOrder } from '../api/order'
import { pageUsers } from '../api/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const users = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, status: null, buyerId: null })

const statusMap = { 0: '待确认', 1: '已成交', 2: '已取消', 3: '已拒绝' }
const statusType = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }

const userMap = computed(() => {
  const map = new Map()
  users.value.forEach(u => map.set(u.id, u.realName || u.username))
  return map
})
const userName = id => userMap.value.get(id) || id

const rejectVisible = ref(false)
const rejecting = ref(false)
const currentRow = ref(null)
const rejectRemark = ref('')

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageOrders(query)
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const u = await pageUsers({ pageNum: 1, pageSize: 100 })
  users.value = u.records
}

const handleConfirm = row => {
  ElMessageBox.confirm(`确认成交《${row.bookName}》？该图书将标记为已售出。`, '成交确认', { type: 'info' })
    .then(async () => {
      try {
        await confirmOrder(row.id, '同意成交')
        ElMessage.success('已成交，图书标记为已售出')
        loadData(1)
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    })
    .catch(() => {})
}

const openReject = row => {
  currentRow.value = row
  rejectRemark.value = ''
  rejectVisible.value = true
}

const handleReject = async () => {
  rejecting.value = true
  try {
    await rejectOrder(currentRow.value.id, rejectRemark.value)
    ElMessage.success('已拒绝该订单')
    rejectVisible.value = false
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

const handleDelete = row => {
  ElMessageBox.confirm('确定删除该订单记录？', '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await deleteOrder(row.id)
        ElMessage.success('删除成功')
        loadData(1)
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadOptions().catch(() => {})
  loadData(1)
})
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>