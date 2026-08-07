const store = require('../../utils/book-store')

const STATUS_TABS = [
  { key: 'all', label: '全部' },
  { key: '0', label: '待确认' },
  { key: '1', label: '已成交' },
  { key: '2', label: '已取消' },
  { key: '3', label: '已拒绝' }
]

Page({
  data: {
    isAdmin: false,
    myId: 0,
    owner: '',
    tabs: STATUS_TABS,
    activeTab: 'all',
    orders: [],
    filtered: []
  },

  onShow() {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const me = store.getUser()
    this.setData({
      isAdmin: store.isAdmin(),
      myId: me ? me.id : 0,
      owner: store.ownerName()
    })
    this.load()
  },

  onPullDownRefresh() {
    this.load().finally(() => wx.stopPullDownRefresh())
  },

  async load() {
    wx.showLoading({ title: '加载中...' })
    try {
      const isAdmin = store.isAdmin()
      const orders = await store.getOrders(isAdmin ? 'all' : undefined)
      this.setData({ orders })
      this.applyFilter()
    } catch (e) {
      // api 层已提示
    } finally {
      wx.hideLoading()
    }
  },

  onTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.key })
    this.applyFilter()
  },

  // 按当前状态 tab 过滤订单（WXML 不支持方法调用，先算好存 data）
  applyFilter() {
    const active = this.data.activeTab
    const filtered = this.data.orders.filter(o => active === 'all' || String(o.status) === active)
    this.setData({ filtered })
  },

  async doCancel(e) {
    try {
      const r = await store.cancelOrder(e.currentTarget.dataset.id)
      wx.showToast({ title: r.msg, icon: 'success' })
      if (r.ok) this.load()
    } catch (err) { /* 已提示 */ }
  },

  async doConfirm(e) {
    try {
      const r = await store.confirmOrder(e.currentTarget.dataset.id)
      wx.showToast({ title: r.msg, icon: 'success' })
      if (r.ok) this.load()
    } catch (err) { /* 已提示 */ }
  },

  async doReject(e) {
    try {
      const r = await store.rejectOrder(e.currentTarget.dataset.id)
      wx.showToast({ title: r.msg, icon: 'none' })
      if (r.ok) this.load()
    } catch (err) { /* 已提示 */ }
  },

  async openDetail(e) {
    const id = e.currentTarget.dataset.bookId
    if (!id) return
    try {
      const book = await store.findBookById(id)
      if (book) {
        wx.navigateTo({
          url: '/pages/detail/detail?isbn=' + encodeURIComponent(book.isbn)
        })
      }
    } catch (err) { /* 已提示 */ }
  }
})
