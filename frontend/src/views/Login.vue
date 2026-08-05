<template>
  <div class="login-page">
    <!-- 动态光晕背景 -->
    <div class="glow glow-1"></div>
    <div class="glow glow-2"></div>
    <div class="glow glow-3"></div>
    <div class="bg-books">
      <span v-for="(b, i) in bookChars" :key="i" :style="b.style">{{ b.char }}</span>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <el-icon :size="34" color="#fff"><Reading /></el-icon>
        </div>
        <h2>二手图书借阅管理系统</h2>
        <p>BOOK MANAGE SYSTEM</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号（英文字母和数字）"
            :prefix-icon="User"
            clearable
            maxlength="20"
            @input="filterUsername"
            @compositionstart="usernameComposing = true"
            @compositionend="usernameComposing = false; filterUsername()"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
            maxlength="20"
            @input="filterPassword"
            @compositionstart="passwordComposing = true"
            @compositionend="passwordComposing = false; filterPassword()"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p>账号仅限英文字母和数字；密码只能使用英文字符、数字与符号，不能包含中文和空格</p>
      </div>

      <div class="login-footer">
        <span>演示账号：admin / 123456</span>
        <div class="register-link">
          没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Reading } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const usernameComposing = ref(false)
const passwordComposing = ref(false)

// 账号：只保留英文字母和数字
const filterUsername = () => {
  if (usernameComposing.value) return
  form.username = form.username.replace(/[^A-Za-z0-9]/g, '')
}

// 密码：只保留 ASCII 可见字符（排除中文、空格、emoji 等）
const filterPassword = () => {
  if (passwordComposing.value) return
  form.password = form.password.replace(/[^\x21-\x7E]/g, '')
}

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      pattern: /^[A-Za-z0-9]+$/,
      message: '账号只能包含英文字母和数字，不能使用汉字',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
    {
      pattern: /^[\x21-\x7E]+$/,
      message: '密码只能使用英文字符、数字和符号，不能包含中文和空格',
      trigger: 'blur'
    }
  ]
}

const handleLogin = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const data = await login(form)
      userStore.setLogin(data.token, data.user)
      ElMessage.success('登录成功，欢迎回来！')
      await nextTick()
      const home = userStore.user?.userType === 1 ? '/dashboard' : '/library'
      router.push(home)
    } catch (e) {
      // 错误提示已在拦截器统一处理
    } finally {
      loading.value = false
    }
  })
}

// 背景装饰字符（书籍相关符号，随机位置/大小/透明度）
const bookChars = Array.from({ length: 14 }, (_, i) => ({
  char: ['📚', '📖', '📕', '📗', '📘', '📙', '✎', '★'][i % 8],
  style: {
    left: `${(i * 7.3 + 5) % 92}%`,
    top: `${(i * 11.7 + 8) % 85}%`,
    fontSize: `${18 + (i % 5) * 10}px`,
    animationDelay: `${i * 0.8}s`,
    opacity: 0.15 + (i % 4) * 0.08
  }
}))
</script>

<style scoped>
.login-page {
  position: relative;
  height: 100%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 40%, #2c5364 100%);
}

/* 动态光晕 */
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.45;
  animation: drift 12s ease-in-out infinite alternate;
}
.glow-1 { width: 420px; height: 420px; background: #409EFF; top: -120px; left: -80px; }
.glow-2 { width: 380px; height: 380px; background: #7c3aed; bottom: -100px; right: -60px; animation-delay: -4s; }
.glow-3 { width: 260px; height: 260px; background: #06b6d4; top: 40%; left: 60%; animation-delay: -8s; }

@keyframes drift {
  0%   { transform: translate(0, 0) scale(1); }
  50%  { transform: translate(40px, -30px) scale(1.15); }
  100% { transform: translate(-30px, 25px) scale(0.95); }
}

/* 漂浮书籍字符 */
.bg-books span {
  position: absolute;
  animation: float 7s ease-in-out infinite;
  user-select: none;
  pointer-events: none;
}
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(-4deg); }
  50%      { transform: translateY(-22px) rotate(5deg); }
}

/* 玻璃拟态卡片 */
.login-card {
  position: relative;
  z-index: 2;
  width: 420px;
  padding: 44px 40px 26px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45);
  animation: cardIn 0.7s ease;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.login-header { text-align: center; margin-bottom: 30px; }
.login-header .logo {
  width: 62px;
  height: 62px;
  margin: 0 auto 16px;
  border-radius: 18px;
  background: linear-gradient(135deg, #409EFF, #7c3aed);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: logoPulse 3s ease-in-out infinite;
}
@keyframes logoPulse {
  0%, 100% { box-shadow: 0 8px 20px rgba(64, 158, 255, 0.45); }
  50%      { box-shadow: 0 8px 30px rgba(124, 58, 237, 0.65); }
}
.login-header h2 { font-size: 21px; color: #1f2d3d; font-weight: 700; letter-spacing: 1px; }
.login-header p { margin-top: 8px; font-size: 11px; color: #909399; letter-spacing: 3px; }

/* 输入框悬浮效果 */
.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  transition: box-shadow 0.25s;
}
.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #a0cfff inset;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 8px;
  font-weight: 600;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #409EFF, #7c3aed);
  transition: transform 0.2s, box-shadow 0.2s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.45);
}

.login-tips {
  margin-top: 4px;
  font-size: 11px;
  line-height: 1.7;
  color: #a8abb2;
  text-align: center;
  border-top: 1px dashed #e4e7ed;
  padding-top: 14px;
}

.login-footer {
  margin-top: 12px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
.register-link { margin-top: 8px; font-size: 13px; color: #909399; }
</style>
