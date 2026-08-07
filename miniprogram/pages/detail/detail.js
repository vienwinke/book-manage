const store = require('../../utils/book-store')
const bookApi = require('../../utils/book-api')

const STATUS_TEXT = { 0: '在售', 1: '已售出', 2: '已下架' }

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
    editForm: { title: '', author: '', category: '', version: '', quality: '', price: '', remark: '' },
    loadingRemote: false,
    remoteMsg: '',
    form: { title: '', author: '', category: '', price: '', remark: '' }
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
      this.fetchRemote()
    }
  },

  // 修复：从其他页返回（如「我的」取消订单后）时刷新状态
  onShow() {
    if (this.data.isbn) this.load()
  },

  load() {
    const book = store.findBook(this.data.isbn)
    if (book) {
      this.setData({
        found: true,
        book: Object.assign({}, book, { statusText: STATUS_TEXT[book.status] || '' }),
        myOrder: store.activeOrderFor(book.isbn),
        canManage: this.data.isAdmin || book.seller === this.data.owner
      })
    } else {
      this.setData({ found: false, book: null, myOrder: null, canManage: false })
    }
  },

  // 扫码后云端自动识别书籍信息
  fetchRemote() {
    this.setData({ loadingRemote: true, remoteMsg: '' })
    bookApi.fetchByIsbn(this.data.isbn)
      .then(info => {
        if (!info || !info.title) {
          this.setData({ loadingRemote: false, remoteMsg: '未从云端识别到该书，可手动填写' })
          return
        }
        if (this.data.found && this.data.book) {
          // 本地已有：合并展示（不覆盖价格/卖家等本地字段）
          const b = Object.assign({}, this.data.book, info)
          b.title = this.data.book.title || info.title
          this.setData({ book: b, loadingRemote: false })
        } else {
          // 本地没有：自动填充表单，仅需确认后上架
          this.setData({
            loadingRemote: false,
            remoteInfo: info,
            'form.title': this.data.form.title || info.title,
            'form.author': this.data.form.author || info.author,
            'form.category': this.data.form.category || info.category,
            'form.cover': info.cover
          })
        }
      })
      .catch(() => this.setData({
        loadingRemote: false,
        remoteMsg: '云端识别失败：开发者工具需勾选「不校验合法域名」'
      }))
  },

  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.' + field]: e.detail.value })
  },

  publish() {
    const f = this.data.form
    if (!f.title.trim()) {
      wx.showToast({ title: '请填写书名', icon: 'none' })
      return
    }
    store.publishBook({
      isbn: this.data.isbn,
      title: f.title,
      author: f.author,
      category: f.category,
      price: f.price,
      remark: f.remark,
      seller: '管理员'
    })
    store.markShelf(this.data.isbn, f.title.trim())
    wx.showToast({ title: '已上架', icon: 'success' })
    this.load()
  },

  shelfOff() {
    const r = store.setBookStatus(this.data.isbn, 2)
    wx.showToast({ title: r.ok ? '已下架' : r.msg, icon: 'none' })
    if (r.ok) this.load()
  },

  removeBook() {
    wx.showModal({
      title: '删除图书',
      content: '确定删除该 ISBN 对应的图书吗？',
      success: res => {
        if (res.confirm) {
          store.removeBook(this.data.isbn)
          store.addHistory(this.data.isbn, (this.data.book && this.data.book.title) || '', 'delete')
          wx.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 600)
        }
      }
    })
  },

  // 重新上架（已下架/已售出 → 在售）
  relist() {
    wx.showModal({
      title: '重新上架',
      content: '确定将这本书重新上架出售吗？',
      success: res => {
        if (!res.confirm) return
        const r = store.setBookStatus(this.data.isbn, 0)
        wx.showToast({ title: r.ok ? '已重新上架' : r.msg, icon: r.ok ? 'success' : 'none' })
        if (r.ok) this.load()
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
        remark: b.remark || ''
      }
    })
  },

  onEditInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['editForm.' + field]: e.detail.value })
  },

  saveEdit() {
    const f = this.data.editForm
    if (!f.title.trim()) {
      wx.showToast({ title: '书名不能为空', icon: 'none' })
      return
    }
    const r = store.updateBook(this.data.isbn, {
      title: f.title,
      author: f.author,
      category: f.category,
      version: f.version,
      quality: f.quality,
      price: f.price,
      remark: f.remark
    })
    wx.showToast({ title: r.ok ? '已保存' : r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) {
      this.setData({ editMode: false })
      this.load()
    }
  },

  cancelEdit() {
    this.setData({ editMode: false })
  },

  buy() {
    const r = store.applyOrder(this.data.isbn, store.ownerName())
    wx.showToast({ title: r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) this.load()
  },

  cancelOrder() {
    const o = this.data.myOrder
    if (!o) return
    const r = store.cancelOrder(o.id)
    wx.showToast({ title: r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) {
      this.setData({ myOrder: null })
      this.load()
    }
  },

  confirmOrder(e) {
    const r = store.confirmOrder(e.currentTarget.dataset.id)
    wx.showToast({ title: r.msg, icon: r.ok ? 'success' : 'none' })
    if (r.ok) this.load()
  },

  rejectOrder(e) {
    const r = store.rejectOrder(e.currentTarget.dataset.id)
    wx.showToast({ title: r.msg, icon: 'none' })
    if (r.ok) this.load()
  }
})