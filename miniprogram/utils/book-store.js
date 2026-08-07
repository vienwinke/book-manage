const api = require('./api')

const HISTORY_KEY = 'ags_history'
const USER_KEY = 'ags_user'

// ---------- 登录态（后端账号密码） ----------
function isLogin() {
  return !!api.getToken()
}

function getUser() {
  return wx.getStorageSync(USER_KEY) || null
}

function isAdmin() {
  const u = getUser()
  return !!u && u.userType === 1
}

function isUser() {
  const u = getUser()
  return !!u && u.userType === 0
}

function ownerName() {
  const u = getUser()
  return (u && (u.realName || u.username)) || ''
}

// 后端登录成功后写入本地登录态
function setLogin(user) {
  wx.setStorageSync(USER_KEY, user)
}

async function login(username, password) {
  const data = await api.post('/auth/login', { username, password })
  if (data && data.token && data.user) {
    api.setToken(data.token)
    setLogin(data.user)
    return { ok: true, user: data.user }
  }
  return { ok: false, msg: '登录失败' }
}

async function logout() {
  try {
    await api.post('/auth/logout')
  } catch (e) { /* 忽略 */ }
  api.clearToken()
  wx.removeStorageSync(USER_KEY)
}

// ---------- 图书（后端） ----------
// 后端字段 → 小程序展示字段
function mapBook(b) {
  return {
    id: b.id,
    isbn: b.isbn || '',
    title: b.bookName || '',
    author: b.author || '',
    category: b.category || '',
    version: b.version || '',
    quality: b.quality || '',
    price: b.price,
    stock: b.stock,
    seller: b.sellerName || '',
    sellerId: b.sellerId,
    status: b.bookStatus,
    remark: b.remark || '',
    cover: ''
  }
}

// 在售列表（书库）
async function onSaleList(keyword) {
  const params = { pageNum: 1, pageSize: 100, bookStatus: 0 }
  if (keyword) params.bookName = keyword
  const data = await api.get('/book/page', params)
  const records = (data && data.records) || []
  const list = records.map(mapBook)
  const kw = (keyword || '').trim().toLowerCase()
  return kw ? list.filter(b =>
    (b.title || '').toLowerCase().includes(kw) ||
    (b.author || '').toLowerCase().includes(kw) ||
    (b.isbn || '').includes(kw) ||
    (b.seller || '').includes(kw)) : list
}

// 全部图书（管理员本地书库）
async function allBooks() {
  const data = await api.get('/book/page', { pageNum: 1, pageSize: 100 })
  return ((data && data.records) || []).map(mapBook)
}

// 按 ISBN 查一本书（详情页/扫码）
async function findBookByIsbn(isbn) {
  if (!isbn) return null
  const data = await api.get('/book/page', { pageNum: 1, pageSize: 1, isbn })
  const records = (data && data.records) || []
  return records.length ? mapBook(records[0]) : null
}

// 按 id 查一本书（订单点击进详情）
async function findBookById(id) {
  if (!id) return null
  const data = await api.get('/book/' + id)
  return data ? mapBook(data) : null
}

// 管理员上架
async function publishBook(form) {
  await api.post('/book/add', {
    bookName: form.title,
    author: form.author,
    isbn: form.isbn,
    category: form.category,
    version: form.version,
    quality: form.quality,
    price: form.price,
    stock: form.stock,
    remark: form.remark,
    bookStatus: 0
  })
  return { ok: true }
}

// 修改图书信息（按 ISBN 找到 id 后更新）
async function updateBookByIsbn(isbn, form) {
  const book = await findBookByIsbn(isbn)
  if (!book) return { ok: false, msg: '图书不存在' }
  await api.put('/book/update', {
    id: book.id,
    bookName: form.title,
    author: form.author,
    category: form.category,
    version: form.version,
    quality: form.quality,
    price: form.price,
    stock: form.stock,
    remark: form.remark
  })
  return { ok: true }
}

// 设置图书状态（下架2/重新上架0/已售1）
async function setBookStatusByIsbn(isbn, status) {
  const book = await findBookByIsbn(isbn)
  if (!book) return { ok: false, msg: '图书不存在' }
  await api.put('/book/update', { id: book.id, bookStatus: status })
  return { ok: true }
}

// 删除图书（按 ISBN）
async function removeBookByIsbn(isbn) {
  const book = await findBookByIsbn(isbn)
  if (!book) return { ok: false, msg: '图书不存在' }
  await api.del('/book/delete/' + book.id)
  return { ok: true }
}

// 我发布的（普通用户卖家身份查）
async function myBooks() {
  const u = getUser()
  if (!u) return []
  const data = await api.get('/book/page', { pageNum: 1, pageSize: 100, sellerId: u.id })
  return ((data && data.records) || []).map(mapBook)
}

// ---------- 订单（后端） ----------
function mapOrder(o) {
  return {
    id: o.id,
    bookId: o.bookId,
    title: o.bookName || '',
    price: o.orderPrice,
    buyer: o.buyerName || '',
    buyerId: o.buyerId,
    seller: o.sellerName || '',
    sellerId: o.sellerId,
    status: o.status,
    statusText: ORDER_STATUS[o.status] || '',
    applyTime: (o.applyTime || '').replace('T', ' ').slice(0, 16),
    doneTime: (o.doneTime || '').replace('T', ' ').slice(0, 16)
  }
}

const ORDER_STATUS = { 0: '待确认', 1: '已成交', 2: '已取消', 3: '已拒绝' }

// 当前用户订单（普通用户后端自动只查自己的，管理员查全部）
async function getOrders(status) {
  const params = { pageNum: 1, pageSize: 100 }
  if (status !== undefined && status !== null && status !== 'all') params.status = status
  const data = await api.get('/order/page', params)
  return ((data && data.records) || []).map(mapOrder)
}

// 某本书的待确认订单（详情页展示）
async function activeOrderFor(isbn) {
  const book = await findBookByIsbn(isbn)
  if (!book) return null
  const data = await api.get('/order/page', { pageNum: 1, pageSize: 1, status: 0 })
  const records = (data && data.records) || []
  const hit = records.find(o => o.bookId === book.id)
  return hit ? mapOrder(hit) : null
}

// 下单购买（isbn → bookId）
async function applyOrder(isbn) {
  const book = await findBookByIsbn(isbn)
  if (!book) return { ok: false, msg: '图书不存在' }
  if (book.status !== 0) return { ok: false, msg: '该书已售出或已下架' }
  await api.post('/order/apply', { bookId: book.id })
  return { ok: true, msg: '已下单，等待管理员确认' }
}

async function cancelOrder(id) {
  await api.post('/order/cancel/' + id)
  return { ok: true, msg: '已取消订单' }
}

async function confirmOrder(id) {
  await api.post('/order/confirm/' + id, {})
  return { ok: true, msg: '已确认成交，图书已售出' }
}

async function rejectOrder(id) {
  await api.post('/order/reject/' + id, {})
  return { ok: true, msg: '已拒绝该订单' }
}

// ---------- 扫码历史（本地） ----------
function getHistory() {
  return wx.getStorageSync(HISTORY_KEY) || []
}

function addHistory(isbn, title, kind) {
  const list = getHistory()
  list.unshift({
    isbn: (isbn || '').trim(),
    title: title || '未知书名',
    shelf: kind === 'shelf',
    time: nowStr()
  })
  wx.setStorageSync(HISTORY_KEY, list.slice(0, 50))
}

function markShelf(isbn, title) {
  const i = (isbn || '').trim()
  const list = getHistory()
  const hit = list.find(h => h.isbn === i)
  if (hit) {
    hit.shelf = true
    if (title) hit.title = title
    wx.setStorageSync(HISTORY_KEY, list)
  }
}

function getStats() {
  const history = getHistory()
  const today = todayStr()
  return {
    today: history.filter(h => h.time.slice(0, 10) === today).length,
    total: history.length,
    shelf: history.filter(h => h.shelf).length
  }
}

function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

function nowStr() {
  const d = new Date()
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
    ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}

function todayStr() {
  return nowStr().slice(0, 10)
}

function timeStr(t) {
  return t || ''
}

module.exports = {
  isLogin,
  getUser,
  isAdmin,
  isUser,
  ownerName,
  login,
  logout,
  setLogin,
  onSaleList,
  findBookByIsbn,
  findBookById,
  allBooks,
  publishBook,
  updateBookByIsbn,
  setBookStatusByIsbn,
  removeBookByIsbn,
  myBooks,
  getOrders,
  activeOrderFor,
  applyOrder,
  cancelOrder,
  confirmOrder,
  rejectOrder,
  getHistory,
  addHistory,
  markShelf,
  getStats,
  timeStr
}
