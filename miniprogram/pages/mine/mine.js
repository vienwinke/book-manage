const store = require('../../utils/book-store')

const BOOK_STATUS = { 0: '在售', 1: '已售出', 2: '已下架' }
const ORDER_STATUS = { 0: '待确认', 1: '已成交', 2: '已取消', 3: '已拒绝' }

Page({
  data: {
    isAdmin: false,
    isUser: false,
    owner: '',
    myOrders: [],
    myListings: [],
    allOrders: [],
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

  decodeOrder(o) {
    return {
      id: o.id,
      title: o.title,
      price: o.price,
      buyer: o.buyer,
      seller: o.seller,
      status: o.status,
      statusText: ORDER_STATUS[o.status] || '',
      applyTime: o.applyTime
    }
  },

  refresh() {
    const owner = store.ownerName()
    this.setData({
      isAdmin: store.isAdmin(),
      isUser: store.isUser(),
      owner,
      myOrders: store.myOrders(owner).map(o => this.decodeOrder(o)),
      myListings: store.myBooks(owner).map(b => ({
        isbn: b.isbn,
        title: b.title,
        price: b.price,
        status: b.status,
        statusText: BOOK_STATUS[b.status] || ''
      })),
      allOrders: store.getOrders().map(o => this.decodeOrder(o)),
      books: store.getBooks().map(b => ({
        isbn: b.isbn,
        title: b.title,
        price: b.price,
        statusText: BOOK_STATUS[b.status] || ''
      })),
      stats: store.getStats()
    })
  },

  doCancel(e) {
    const r = store.cancelOrder(e.currentTarget.dataset.id)
    wx.showToast({ title: r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) this.refresh()
  },

  doConfirm(e) {
    const r = store.confirmOrder(e.currentTarget.dataset.id)
    wx.showToast({ title: r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) this.refresh()
  },

  doReject(e) {
    const r = store.rejectOrder(e.currentTarget.dataset.id)
    wx.showToast({ title: r.msg, icon: 'none' })
    if (r.ok) this.refresh()
  },

  shelfOff(e) {
    const r = store.setBookStatus(e.currentTarget.dataset.isbn, 2)
    wx.showToast({ title: r.ok ? '已下架' : r.msg, icon: 'none' })
    if (r.ok) this.refresh()
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
      success: res => {
        if (res.confirm) {
          store.logout()
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})