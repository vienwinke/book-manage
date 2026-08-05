<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.bookName" placeholder="书名" clearable style="width: 180px" @keyup.enter="loadData(1)" />
      <el-select v-model="query.category" placeholder="分类" clearable style="width: 130px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookName" label="书名" min-width="150" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="110" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="version" label="版次" width="80" />
      <el-table-column prop="quality" label="成色" width="90" />
      <el-table-column prop="availableNum" label="可借" width="80" align="center" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="row.availableNum <= 0 || row.bookStatus !== 0"
            @click="openApply(row)"
          >
            {{ row.availableNum > 0 && row.bookStatus === 0 ? '申请借阅' : '不可借' }}
          </el-button>
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

    <!-- 申请借阅弹窗 -->
    <el-dialog v-model="dialogVisible" title="申请借阅" width="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="书名">{{ currentBook.bookName }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentBook.author }}</el-descriptions-item>
        <el-descriptions-item label="可借数量">{{ currentBook.availableNum }}</el-descriptions-item>
        <el-descriptions-item label="预计归还日期">
          <el-date-picker
            v-model="expectReturnTime"
            type="date"
            placeholder="选择日期"
            :disabled-date="d => d.getTime() < Date.now() - 86400000"
            value-format="YYYY-MM-DD"
          />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageBooks } from '../api/book'
import { applyBorrow } from '../api/borrow'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, bookName: '', category: '', bookStatus: 0 })

const categories = ['计算机', '教材', '小说', '文学', '历史', '科学', '其他']

const dialogVisible = ref(false)
const applying = ref(false)
const currentBook = ref({})
const expectReturnTime = ref('')

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageBooks(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openApply = row => {
  currentBook.value = row
  expectReturnTime.value = ''
  dialogVisible.value = true
}

const handleApply = async () => {
  if (!expectReturnTime.value) {
    ElMessage.warning('请选择预计归还日期')
    return
  }
  applying.value = true
  try {
    await applyBorrow({
      userId: userStore.user.id,
      bookId: currentBook.value.id,
      expectReturnTime: expectReturnTime.value
    })
    ElMessage.success('申请已提交，等待管理员审核')
    dialogVisible.value = false
    loadData()
  } finally {
    applying.value = false
  }
}

onMounted(() => loadData(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
