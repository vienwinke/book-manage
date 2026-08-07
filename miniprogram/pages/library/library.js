const store = require('../../utils/book-store')

Page({
  data: {
    keyword: '',
    books: []
  },

  onShow() {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.render()
  },

  onSearch(e) {
    this.setData({ keyword: e.detail.value }, () => this.render())
  },

  async render() {
    wx.showLoading({ title: '加载中...' })
    try {
      const list = await store.onSaleList(this.data.keyword)
      this.setData({ books: list })
    } finally {
      wx.hideLoading()
    }
  },

  openDetail(e) {
    const isbn = e.currentTarget.dataset.isbn
    wx.navigateTo({
      url: '/pages/detail/detail?isbn=' + encodeURIComponent(isbn)
    })
  }
})
