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

  render() {
    const kw = this.data.keyword.trim().toLowerCase()
    const list = store.onSaleList()
      .filter(b => !kw || (b.title || '').toLowerCase().includes(kw) ||
        (b.author || '').toLowerCase().includes(kw) || (b.seller || '').includes(kw) || b.isbn.includes(kw))
    this.setData({ books: list })
  },

  openDetail(e) {
    const isbn = e.currentTarget.dataset.isbn
    wx.navigateTo({
      url: '/pages/detail/detail?isbn=' + encodeURIComponent(isbn)
    })
  }
})