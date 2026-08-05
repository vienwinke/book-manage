<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="loadData(1)">
        <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
      </el-select>
      <el-select v-model="query.userId" placeholder="借阅人" clearable filterable style="width: 160px" @change="loadData(1)">
        <el-option v-for="u in users" :key="u.id" :label="u.realName || u.username" :value="u.id" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="借阅人" width="110">
        <template #default="{ row }">{{ userName(row.userId) }}</template>
      </el-table-column>
      <el-table-column label="图书" min-width="140">
        <template #default="{ row }">{{ bookName(row.bookId) }}</template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="165" />
      <el-table-column prop="expectReturnTime" label="预计归还" width="110" />
      <el-table-column prop="actualReturnTime" label="实际归还" width="165" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminRemark" label="备注" width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button link type="danger" @click="openReject(row)">拒绝</el-button>
          </template>
          <template v-else-if="row.status === 1">
            <el-button link type="primary" @click="handleReturn(row)">标记归还</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="rejectVisible" title="拒绝借阅" width="400px">
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
import { pageBorrows, approveBorrow, rejectBorrow, returnBorrow, deleteBorrow } from '../api/borrow'
import { pageBooks } from '../api/book'
import { pageUsers } from '../api/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const users = ref([])
const books = ref([])
const optionsLoaded = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, status: null, userId: null })

const statusMap = { 0: '待审核', 1: '借出中', 2: '已归还', 3: '已拒绝' }
const statusType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }

const userMap = computed(() => {
  const map = new Map()
  users.value.forEach(u => map.set(u.id, u.realName || u.username))
  return map
})

const bookMap = computed(() => {
  const map = new Map()
  books.value.forEach(b => map.set(b.id, b.bookName))
  return map
})

const userName = id => userMap.value.get(id) || id
const bookName = id => bookMap.value.get(id) || id

const rejectVisible = ref(false)
const rejecting = ref(false)
const currentRow = ref(null)
const rejectRemark = ref('')

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageBorrows(query)
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error(e.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [u, b] = await Promise.all([
    pageUsers({ pageNum: 1, pageSize: 100 }),
    pageBooks({ pageNum: 1, pageSize: 100 })
  ])
  users.value = u.records
  books.value = b.records
  optionsLoaded.value = true
}

const handleApprove = row => {
  ElMessageBox.confirm(`确认通过《${bookName(row.bookId)}》的借阅申请？`, '审核确认', { type: 'info' })
    .then(async () => {
      try {
        await approveBorrow(row.id, '同意')
        ElMessage.success('已通过，图书借出')
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
    await rejectBorrow(currentRow.value.id, rejectRemark.value)
    ElMessage.success('已拒绝该申请')
    rejectVisible.value = false
    loadData(1)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

const handleReturn = row => {
  ElMessageBox.confirm(`确认《${bookName(row.bookId)}》已归还？`, '归还确认', { type: 'info' })
    .then(async () => {
      try {
        await returnBorrow(row.id)
        ElMessage.success('已标记归还，库存已恢复')
        loadData(1)
      } catch (e) {
        ElMessage.error(e.message || '操作失败')
      }
    })
    .catch(() => {})
}

const handleDelete = row => {
  ElMessageBox.confirm('确定删除该记录？', '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await deleteBorrow(row.id)
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
