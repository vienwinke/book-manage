const api = require('../../utils/api')

Page({
  data: {
    username: '',
    realName: '',
    password: '',
    confirm: '',
    loading: false
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onRealNameInput(e) {
    this.setData({ realName: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  onConfirmInput(e) {
    this.setData({ confirm: e.detail.value })
  },

  async doRegister() {
    const username = this.data.username.trim()
    const realName = this.data.realName.trim()
    const password = this.data.password
    const confirm = this.data.confirm
    if (!username || !realName || !password || !confirm) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    if (password !== confirm) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      await api.post('/auth/register', { username, password, realName })
      wx.showToast({ title: '注册成功，请登录', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 800)
    } catch (e) {
      // 错误已由 api 层提示
    } finally {
      this.setData({ loading: false })
    }
  }
})
