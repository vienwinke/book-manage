const store = require('../../utils/book-store')

const STATUS_TEXT = { 0: '在售', 1: '已售出', 2: '已下架' }

function bookStatusText(b) {
  if (b.status === 0 && b.stock <= 0) return '缺货'
  return STATUS_TEXT[b.status] || ''
}

Page({
  data: {
    isbn: '',
    book: null,
    found: false,
    isAdmin: false,
    isUser: false,
    owner: '',
    canManage: false,
    myOrder: null,
    editMode: false,
    editForm: { title: '', author: '', category: '', version: '', quality: '', price: '', stock: '', remark: '' },
    form: { title: '', author: '', category: '', price: '', stock: '', remark: '' }
  },

  onLoad(query) {
    if (!store.isLogin()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const isbn = decodeURIComponent(query.isbn || '')
    this.setData({
      isbn,
      isAdmin: store.isAdmin(),
      isUser: store.isUser(),
      owner: store.ownerName(),
      form: { title: '', author: '', category: '', price: '', remark: '' }
    })
    if (isbn) {
      this.load()
    }
  },

  // 修复：从其他页返回（如「我的」取消订单后）时刷新状态
  onShow() {
    if (this.data.isbn) this.load()
  },

  async load() {
    const isbn = this.data.isbn
    if (!isbn) return
    try {
      const book = await store.findBookByIsbn(isbn)
      if (book) {
        book.statusText = bookStatusText(book)
        const myOrder = await store.activeOrderFor(book.isbn)
        const me = store.getUser()
        const isOwner = !!(me && me.id && book.sellerId === me.id)
        this.setData({
          found: true,
          book,
          myOrder,
          canManage: this.data.isAdmin || isOwner
        })
      } else {
        this.setData({ found: false, book: null, myOrder: null, canManage: false })
      }
    } catch (e) {
      // api 层已提示
    }
  },

  // 扫码后从本地库（后端 book_info 表）查书
  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  async publish() {
    const f = this.data.form
    if (!f.title.trim()) {
      wx.showToast({ title: '请填写书名', icon: 'none' })
      return
    }
    try {
      const r = await store.publishBook({
        isbn: this.data.isbn,
        title: f.title,
        author: f.author,
        category: f.category,
        version: f.version,
        quality: f.quality,
        price: f.price,
        stock: f.stock,
        remark: f.remark
      })
      store.markShelf(this.data.isbn, f.title.trim())
      wx.showToast({ title: '已上架', icon: 'success' })
      this.load()
    } catch (e) { /* 已提示 */ }
  },

  async shelfOff() {
    try {
      await store.setBookStatusByIsbn(this.data.isbn, 2)
      wx.showToast({ title: '已下架', icon: 'success' })
      this.load()
    } catch (e) { /* 已提示 */ }
  },

  async removeBook() {
    wx.showModal({
      title: '删除图书',
      content: '确定删除该 ISBN 对应的图书吗？',
      success: async res => {
        if (res.confirm) {
          try {
            await store.removeBookByIsbn(this.data.isbn)
            store.addHistory(this.data.isbn, (this.data.book && this.data.book.title) || '', 'delete')
            wx.showToast({ title: '已删除', icon: 'success' })
            setTimeout(() => wx.navigateBack(), 600)
          } catch (e) { /* 已提示 */ }
        }
      }
    })
  },

  // 重新上架（已下架/已售出 → 在售）
  async relist() {
    wx.showModal({
      title: '重新上架',
      content: '确定将这本书重新上架出售吗？',
      success: async res => {
        if (!res.confirm) return
        try {
          await store.setBookStatusByIsbn(this.data.isbn, 0)
          wx.showToast({ title: '已重新上架', icon: 'success' })
          this.load()
        } catch (e) { /* 已提示 */ }
      }
    })
  },

  startEdit() {
    const b = this.data.book
    if (!b) return
    this.setData({
      editMode: true,
      editForm: {
        title: b.title,
        author: b.author || '',
        category: b.category || '',
        version: b.version || '',
        quality: b.quality || '',
        price: String(b.price),
        stock: b.stock == null ? '' : String(b.stock),
        remark: b.remark || ''
      }
    })
  },

  onEditInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['editForm.' + field]: e.detail.value })
  },

  async saveEdit() {
    const f = this.data.editForm
    if (!f.title.trim()) {
      wx.showToast({ title: '书名不能为空', icon: 'none' })
      return
    }
    try {
      const r = await store.updateBookByIsbn(this.data.isbn, {
        title: f.title,
        author: f.author,
        category: f.category,
        version: f.version,
        quality: f.quality,
        price: f.price,
        stock: f.stock,
        remark: f.remark
      })
      if (!r.ok) {
        wx.showToast({ title: r.msg, icon: 'none' })
        return
      }
      wx.showToast({ title: '已保存', icon: 'success' })
      this.setData({ editMode: false })
      this.load()
    } catch (e) { /* 已提示 */ }
  },

  cancelEdit() {
    this.setData({ editMode: false })
  },

  async buy() {
    try {
      const r = await store.applyOrder(this.data.isbn)
      wx.showToast({ title: r.msg, icon: 'success' })
      if (r.ok) this.load()
    } catch (e) { /* 已提示 */ }
  },

  async cancelOrder() {
    const o = this.data.myOrder
    if (!o) return
    try {
      const r = await store.cancelOrder(o.id)
      wx.showToast({ title: r.msg, icon: 'success' })
      if (r.ok) {
        this.setData({ myOrder: null })
        this.load()
      }
    } catch (e) { /* 已提示 */ }
  },

  async confirmOrder(e) {
    try {
      const r = await store.confirmOrder(e.currentTarget.dataset.id)
      wx.showToast({ title: r.msg, icon: 'success' })
      if (r.ok) this.load()
    } catch (e) { /* 已提示 */ }
  },

  async rejectOrder(e) {
    try {
      const r = await store.rejectOrder(e.currentTarget.dataset.id)
      wx.showToast({ title: r.msg, icon: 'none' })
      if (r.ok) this.load()
    } catch (e) { /* 已提示 */ }
  }
})
