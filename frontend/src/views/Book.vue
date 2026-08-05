<template>
  <el-card shadow="never">
    <!-- 搜索栏 -->
    <div class="toolbar">
      <el-input v-model="query.bookName" placeholder="书名" clearable style="width: 160px" @keyup.enter="loadData(1)" />
      <el-input v-model="query.author" placeholder="作者" clearable style="width: 140px" @keyup.enter="loadData(1)" />
      <el-select v-model="query.category" placeholder="分类" clearable style="width: 130px">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="query.bookStatus" placeholder="状态" clearable style="width: 110px">
        <el-option label="上架" :value="0" />
        <el-option label="下架" :value="1" />
      </el-select>
      <el-button type="primary" @click="loadData(1)">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openForm()">新增图书</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookName" label="书名" min-width="140" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="110" show-overflow-tooltip />
      <el-table-column prop="isbn" label="ISBN" width="130" />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="version" label="版次" width="80" />
      <el-table-column prop="quality" label="成色" width="90" />
      <el-table-column prop="totalNum" label="总数" width="70" align="center" />
      <el-table-column prop="availableNum" label="可借" width="70" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.bookStatus === 0 ? 'success' : 'info'">
            {{ row.bookStatus === 0 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="90" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑图书' : '新增图书'" width="560px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="书名" prop="bookName">
              <el-input v-model="form.bookName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="form.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN">
              <el-input v-model="form.isbn" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="form.category" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="版次">
              <el-input v-model="form.version" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色">
              <el-input v-model="form.quality" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalNum">
              <el-input-number v-model="form.totalNum" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可借数量" prop="availableNum">
              <el-input-number v-model="form.availableNum" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.bookStatus">
                <el-radio :value="0">上架</el-radio>
                <el-radio :value="1">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-input v-model="form.source" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { pageBooks, addBook, updateBook, deleteBook } from '../api/book'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, bookName: '', author: '', category: '', bookStatus: null })

const categories = ['计算机', '教材', '小说', '文学', '历史', '科学', '其他']

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const emptyForm = () => ({
  id: null, bookName: '', author: '', isbn: '', category: '', version: '',
  quality: '', totalNum: 0, availableNum: 0, bookStatus: 0, source: '', remark: ''
})
const form = reactive(emptyForm())

const formRules = {
  bookName: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  totalNum: [{ required: true, message: '请输入总数量', trigger: 'change' }]
}

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

const resetQuery = () => {
  Object.assign(query, { pageNum: 1, bookName: '', author: '', category: '', bookStatus: null })
  loadData(1)
}

const openForm = row => {
  Object.assign(form, emptyForm(), row || {})
  if (form.id) {
    form._originTotalNum = form.totalNum
  }
  dialogVisible.value = true
}

const handleSave = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        const diff = form.totalNum - (form._originTotalNum || 0)
        if (diff !== 0) {
          form.availableNum = Math.max(0, form.availableNum + diff)
        }
        await updateBook({ ...form })
        ElMessage.success('修改成功')
      } else {
        if (form.availableNum === 0 && form.totalNum > 0) {
          form.availableNum = form.totalNum
        }
        await addBook({ ...form })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = row => {
  ElMessageBox.confirm(`确定删除图书《${row.bookName}》吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => loadData(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
