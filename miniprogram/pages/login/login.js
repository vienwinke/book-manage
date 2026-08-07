const store = require('../../utils/book-store')

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onLoad() {
    if (store.isLogin()) {
      wx.reLaunch({ url: '/pages/index/index' })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  },

  async doLogin() {
    const username = this.data.username.trim()
    const password = this.data.password
    if (!username || !password) {
      wx.showToast({ title: '请输入账号和密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const r = await store.login(username, password)
      if (r.ok) {
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => wx.reLaunch({ url: '/pages/index/index' }), 500)
      }
    } catch (e) {
      // 错误已由 api 层提示
    } finally {
      this.setData({ loading: false })
    }
  }
})
