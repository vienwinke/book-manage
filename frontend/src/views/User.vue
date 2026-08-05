<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width: 160px" @keyup.enter="loadData(1)" />
      <el-select v-model="query.userType" placeholder="角色" clearable style="width: 120px" @change="loadData(1)">
        <el-option label="管理员" :value="1" />
        <el-option label="学生" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column label="角色" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.userType === 1 ? 'danger' : 'success'">
            {{ row.userType === 1 ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :disabled="row.id === userStore.user?.id"
            @change="val => toggleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="175" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            link
            type="danger"
            :disabled="row.id === userStore.user?.id"
            @click="handleDelete(row)"
          >
            删除
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.userType">
            <el-radio :value="1">管理员</el-radio>
            <el-radio :value="0">学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, updateUser, deleteUser } from '../api/user'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', userType: null })

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, username: '', realName: '', userType: 0, status: 1 })

const loadData = async pageNum => {
  if (pageNum) query.pageNum = pageNum
  loading.value = true
  try {
    const data = await pageUsers(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(query, { pageNum: 1, username: '', userType: null })
  loadData(1)
}

const openEdit = row => {
  Object.assign(form, { id: row.id, username: row.username, realName: row.realName, userType: row.userType, status: row.status })
  dialogVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateUser({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row, val) => {
  await updateUser({ id: row.id, status: val ? 1 : 0 })
  row.status = val ? 1 : 0
  ElMessage.success(val ? '已启用' : '已禁用')
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定删除用户 ${row.username} 吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
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
