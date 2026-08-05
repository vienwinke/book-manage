import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：注入 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 响应拦截：统一处理业务码
request.interceptors.response.use(
  res => {
    const { code, msg, data } = res.data
    if (code === 200) {
      return data
    }
    if (code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.error(msg || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(msg))
    }
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  err => {
    ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)

export default request
