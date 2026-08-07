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
      <el-table-column label="售价" width="90" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sellerName" label="卖家" width="100" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="row.bookStatus !== 0"
            @click="openApply(row)"
          >
            {{ row.bookStatus === 0 ? '下单购买' : '已售出' }}
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

    <!-- 下单确认弹窗 -->
    <el-dialog v-model="dialogVisible" title="确认下单" width="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="书名">{{ currentBook.bookName }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentBook.author }}</el-descriptions-item>
        <el-descriptions-item label="售价">
          <span style="color: #f56c6c; font-weight: 700; font-size: 18px">¥{{ currentBook.price }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="卖家">{{ currentBook.sellerName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="handleApply">确认下单</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageBooks } from '../api/book'
import { applyOrder } from '../api/order'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, bookName: '', category: '', bookStatus: 0 })

const categories = ['计算机', '教材', '小说', '文学', '历史', '科学', '其他']

const dialogVisible = ref(false)
const applying = ref(false)
const currentBook = ref({})

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
  dialogVisible.value = true
}

const handleApply = async () => {
  applying.value = true
  try {
    await applyOrder(currentBook.value.id)
    ElMessage.success('下单成功，等待卖家确认成交')
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