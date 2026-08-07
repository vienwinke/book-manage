// 后端接口请求封装：统一 baseURL、token 注入、错误处理
// 开发者工具模拟器可用 127.0.0.1；真机预览必须改为电脑的局域网 IP（如 http://192.168.x.x:8080）
const BASE_URL = 'http://192.168.1.6:8080'
const TOKEN_KEY = 'ags_token'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
}

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: method,
      data: data,
      timeout: 10000,
      header: {
        'Content-Type': 'application/json',
        'satoken': getToken()
      },
      success: res => {
        const body = res.data || {}
        if (body.code === 200) {
          resolve(body.data)
          return
        }
        if (body.code === 401) {
          clearToken()
          wx.removeStorageSync('ags_user')
          wx.reLaunch({ url: '/pages/login/login' })
        }
        wx.showToast({ title: body.msg || '请求失败', icon: 'none' })
        reject(new Error(body.msg || '请求失败'))
      },
      fail: err => {
        wx.showToast({ title: '网络错误，请确认后端已启动', icon: 'none' })
        reject(err)
      }
    })
  })
}

const get = (url, data) => request('GET', url, data)
const post = (url, data) => request('POST', url, data)
const put = (url, data) => request('PUT', url, data)
const del = (url, data) => request('DELETE', url, data)

module.exports = {
  BASE_URL,
  getToken,
  setToken,
  clearToken,
  get,
  post,
  put,
  del
}
