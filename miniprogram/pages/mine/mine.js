const store = require('../../utils/book-store')

const BOOK_STATUS = { 0: '在售', 1: '已售出', 2: '已下架' }

Page({
  data: {
    isAdmin: false,
    isUser: false,
    owner: '',
    myListings: [],
    books: [],
    stats: { today: 0, total: 0, shelf: 0 }
  },

  onShow() {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.refresh()
  },

  async refresh() {
    wx.showLoading({ title: '加载中...' })
    try {
      const owner = store.ownerName()
      const isAdmin = store.isAdmin()
      const isUser = store.isUser()
      let myListings = []
      let books = []
      if (isUser) {
        const listings = await store.myBooks()
        myListings = listings.map(b => ({
          isbn: b.isbn,
          title: b.title,
          price: b.price,
          stock: b.stock,
          status: b.status,
          statusText: BOOK_STATUS[b.status] || ''
        }))
      } else if (isAdmin) {
        const allBooks = await store.allBooks()
        books = allBooks.map(b => ({
          isbn: b.isbn,
          title: b.title,
          price: b.price,
          stock: b.stock,
          status: b.status,
          statusText: BOOK_STATUS[b.status] || ''
        }))
      }
      this.setData({
        isAdmin,
        isUser,
        owner,
        myListings,
        books,
        stats: store.getStats()
      })
    } catch (e) {
      // api 层已提示
    } finally {
      wx.hideLoading()
    }
  },

  goOrders() {
    wx.navigateTo({ url: '/pages/orders/orders' })
  },

  async shelfOff(e) {
    try {
      const r = await store.setBookStatusByIsbn(e.currentTarget.dataset.isbn, 2)
      wx.showToast({ title: '已下架', icon: 'success' })
      if (r.ok) this.refresh()
    } catch (e) { /* 已提示 */ }
  },

  openDetail(e) {
    wx.navigateTo({
      url: '/pages/detail/detail?isbn=' + encodeURIComponent(e.currentTarget.dataset.isbn)
    })
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定退出当前账号吗？',
      success: async res => {
        if (res.confirm) {
          await store.logout()
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})
