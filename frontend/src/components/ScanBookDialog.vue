<template>
  <el-dialog v-model="dialogVisible" title="📷 扫码上架图书" width="560px"
             :close-on-click-modal="false" @closed="stopScanner">
    <div id="scan-reader" ref="readerRef" class="reader"></div>
    <div class="tip">将图书 ISBN 条码对准扫描框，识别后自动填入下方表单；无摄像头可手动输入 ISBN</div>

    <el-form :model="form" label-width="80px">
      <el-form-item label="ISBN" required>
        <el-input v-model="form.isbn" placeholder="扫码自动填入，也可手动输入" />
      </el-form-item>
      <el-form-item label="书名" required>
        <el-input v-model="form.bookName" placeholder="请输入书名" />
      </el-form-item>
      <el-form-item label="作者">
        <el-input v-model="form.author" placeholder="选填" />
      </el-form-item>
      <el-form-item label="分类">
        <el-input v-model="form.category" placeholder="选填，如：计算机、教材" />
      </el-form-item>
      <el-form-item label="售价" required>
        <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="success" :loading="saving" @click="submit">添加到库</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { Html5Qrcode } from 'html5-qrcode'
import { ElMessage } from 'element-plus'
import { addBook } from '../api/book'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = ref(props.modelValue)
watch(() => props.modelValue, v => {
  dialogVisible.value = v
  if (v) {
    resetForm()
    nextTick(startScanner)
  }
})

const readerRef = ref(null)
let scanner = null

const form = ref({ isbn: '', bookName: '', author: '', category: '', price: 0.01, remark: '' })
const saving = ref(false)

function resetForm() {
  form.value = { isbn: '', bookName: '', author: '', category: '', price: 0.01, remark: '' }
}

async function startScanner() {
  try {
    if (!scanner) scanner = new Html5Qrcode('scan-reader')
    const cameras = await Html5Qrcode.getCameras()
    if (!cameras || cameras.length === 0) return
    await scanner.start(
      cameras[cameras.length - 1].id,
      { fps: 10, qrbox: { width: 250, height: 150 } },
      text => { form.value.isbn = (text || '').trim() },
      () => {}
    )
  } catch (e) {
    console.warn('扫码启动失败:', e)
  }
}

function stopScanner() {
  if (scanner) {
    scanner.stop().catch(() => {})
  }
}

async function submit() {
  if (!form.value.isbn.trim()) {
    ElMessage.warning('请填写 ISBN')
    return
  }
  if (!form.value.bookName.trim()) {
    ElMessage.warning('请填写书名')
    return
  }
  saving.value = true
  try {
    await addBook({
      bookName: form.value.bookName,
      author: form.value.author,
      isbn: form.value.isbn,
      category: form.value.category,
      price: form.value.price,
      stock: 1,
      bookStatus: 0,
      remark: form.value.remark
    })
    ElMessage.success('上架成功')
    close()
    emit('success')
  } finally {
    saving.value = false
  }
}

function close() {
  stopScanner()
  dialogVisible.value = false
  emit('update:modelValue', false)
}

onBeforeUnmount(() => {
  if (scanner) {
    scanner.stop().catch(() => {})
    scanner.clear()
    scanner = null
  }
})
</script>

<style scoped>
.reader {
  width: 100%;
  min-height: 200px;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}
.tip {
  font-size: 12px;
  color: #909399;
  margin: 8px 0 12px;
  text-align: center;
}
</style>