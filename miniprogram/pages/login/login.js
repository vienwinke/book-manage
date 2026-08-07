const store = require('../../utils/book-store')

Page({
  data: {
    role: '',
    name: ''
  },

  onLoad() {
    if (store.isLogin()) {
      wx.reLaunch({ url: '/pages/index/index' })
    }
  },

  selectRole(e) {
    this.setData({ role: e.currentTarget.dataset.role })
  },

  onNameInput(e) {
    this.setData({ name: e.detail.value })
  },

  doLogin() {
    if (!this.data.role) return
    store.setLogin(this.data.role, this.data.name)
    wx.reLaunch({ url: '/pages/index/index' })
  }
})