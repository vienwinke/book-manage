const store = require('../../utils/book-store')

Page({
  data: {
    isbn: '',
    history: [],
    stats: { today: 0, total: 0, shelf: 0 },
    isAdmin: false
  },

  onShow() {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.refresh()
  },

  refresh() {
    const history = store.getHistory().map(h => ({
      isbn: h.isbn,
      title: h.title,
      shelf: h.shelf,
      timeText: store.timeStr(h.time)
    }))
    this.setData({
      history,
      stats: store.getStats(),
      isAdmin: store.isAdmin()
    })
  },

  onIsbnInput(e) {
    this.setData({ isbn: e.detail.value })
  },

  scan() {
    wx.scanCode({
      scanType: ['barCode', 'qrCode'],
      success: res => {
        const isbn = String(res.result || '').trim()
        if (!isbn) {
          wx.showToast({ title: '未识别到条码', icon: 'none' })
          return
        }
        this.lookup(isbn)
      },
      fail: () => {
        wx.showToast({ title: '已取消或无法访问相机', icon: 'none' })
      }
    })
  },

  lookupByInput() {
    const isbn = this.data.isbn.trim()
    if (!isbn) {
      wx.showToast({ title: '请输入 ISBN', icon: 'none' })
      return
    }
    this.lookup(isbn)
  },

  lookup(isbn) {
    const book = store.findBook(isbn)
    store.addHistory(isbn, book ? book.title : '未收录', 'scan')
    this.refresh()
    wx.navigateTo({
      url: '/pages/detail/detail?isbn=' + encodeURIComponent(isbn)
    })
  },

  openDetail(e) {
    const isbn = e.currentTarget.dataset.isbn
    wx.navigateTo({
      url: '/pages/detail/detail?isbn=' + encodeURIComponent(isbn)
    })
  }
})