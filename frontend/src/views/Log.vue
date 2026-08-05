<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-select v-model="query.logType" placeholder="日志类型" clearable style="width: 140px" @change="loadData(1)">
        <el-option v-for="(label, val) in typeMap" :key="val" :label="label" :value="Number(val)" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="userId" label="操作人ID" width="90" align="center" />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag[row.logType]">{{ typeMap[row.logType] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="操作内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="operateTime" label="操作时间" width="175" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
import { pageLogs, deleteLog } from '../api/log'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, logType: null })

const typeMap = { 0: '查询', 1: '新增', 2: '修改', 3: '删除', 4: '登录', 5: '其他' }
const typeTag = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'primary', 5: 'info' }

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageLogs(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleDelete = row => {
  ElMessageBox.confirm('确定删除该条日志？', '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteLog(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => loadData(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
