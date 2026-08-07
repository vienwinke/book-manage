const store = require('./utils/book-store')

App({
  onLaunch() {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  }
})