const BOOKS_KEY = 'ags_books'
const HISTORY_KEY = 'ags_history'
const ORDER_KEY = 'ags_orders'
const LOGIN_KEY = 'ags_login'

// 内置示例：在售二手书（每本=一个在售条目，含价格与卖家）
const BUILTIN_BOOKS = [
  {
    isbn: '9787111213826',
    title: 'Java编程思想',
    author: 'Bruce Eckel',
    category: '计算机',
    version: '第4版',
    quality: '轻微磨损',
    price: 25,
    seller: '王学长',
    status: 0,
    remark: '少量划线笔记'
  },
  {
    isbn: '9787040091314',
    title: '高等数学上册',
    author: '同济大学',
    category: '教材',
    version: '第七版',
    quality: '较旧',
    price: 12,
    seller: '李学姐',
    status: 0,
    remark: '无缺页'
  },
  {
    isbn: '9787536692930',
    title: '三体',
    author: '刘慈欣',
    category: '科幻',
    version: '1版',
    quality: '全新',
    price: 15,
    seller: '张学长',
    status: 0,
    remark: '无笔记'
  },
  {
    isbn: '9787544269982',
    title: '百年孤独',
    author: '加西亚·马尔克斯',
    category: '文学',
    version: '新版',
    quality: '破损',
    price: 6,
    seller: '李学姐',
    status: 0,
    remark: '封底撕裂'
  },
  {
    isbn: '9787302147510',
    title: '数据结构（C语言版）',
    author: '严蔚敏',
    category: '计算机',
    version: 'C语言版',
    quality: '轻微磨损',
    price: 18,
    seller: '王学长',
    status: 0,
    remark: ''
  },
  {
    isbn: '9787020002207',
    title: '活着',
    author: '余华',
    category: '文学',
    version: '精装',
    quality: '全新',
    price: 10,
    seller: '张学长',
    status: 0,
    remark: ''
  },
  {
    isbn: '9787111600900',
    title: '深入理解计算机系统',
    author: 'Randal E. Bryant',
    category: '计算机',
    version: '原书第3版',
    quality: '轻微磨损',
    price: 35,
    seller: '王学长',
    status: 0,
    remark: ''
  }
]

// ---------- 工具 ----------
function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

// 统一使用本地时区字符串，避免 UTC 日期偏移
function nowStr() {
  const d = new Date()
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) +
    ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}

function todayStr() {
  return nowStr().slice(0, 10)
}

function trimIsbn(isbn) {
  return String(isbn || '').trim()
}

function clone(obj) {
  return JSON.parse(JSON.stringify(obj))
}

function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
}

// ---------- 图书（在售条目） ----------
function getBooks() {
  let list = wx.getStorageSync(BOOKS_KEY)
  if (!list || !list.length) {
    list = clone(BUILTIN_BOOKS)
    wx.setStorageSync(BOOKS_KEY, list)
  }
  return list
}

function saveBooks(list) {
  wx.setStorageSync(BOOKS_KEY, list)
}

function findBook(isbn) {
  const i = trimIsbn(isbn)
  if (!i) return null
  return getBooks().find(b => b.isbn === i) || null
}

function onSaleList() {
  return getBooks().filter(b => b.status === 0)
}

function publishBook(data) {
  const isbn = trimIsbn(data.isbn)
  const list = getBooks()
  const idx = list.findIndex(b => b.isbn === isbn)
  const book = {
    isbn: isbn,
    title: (data.title || '').trim() || '未知书名',
    author: (data.author || '').trim() || '未知',
    category: (data.category || '').trim() || '未分类',
    version: data.version || '',
    quality: data.quality || '',
    price: Math.max(0, Number(data.price) || 0),
    seller: (data.seller || '').trim() || '同学',
    cover: data.cover || '',
    publisher: data.publisher || '',
    publishedDate: data.publishedDate || '',
    remark: (data.remark || '').trim(),
    status: 0,
    createTime: nowStr()
  }
  if (idx >= 0) {
    Object.assign(list[idx], book)
  } else {
    list.unshift(book)
  }
  saveBooks(list)
  return book
}

function setBookStatus(isbn, status) {
  const list = getBooks()
  const idx = list.findIndex(b => b.isbn === trimIsbn(isbn))
  if (idx < 0) return { ok: false, msg: '图书不存在' }
  list[idx].status = status
  saveBooks(list)
  return { ok: true }
}

// 修改图书信息（书名/作者/分类/版次/成色/价格/备注，传入的字段才更新）
function updateBook(isbn, data) {
  const list = getBooks()
  const idx = list.findIndex(b => b.isbn === trimIsbn(isbn))
  if (idx < 0) return { ok: false, msg: '图书不存在' }
  const b = list[idx]
  if (data.title !== undefined) b.title = (data.title || '').trim() || b.title
  if (data.author !== undefined) b.author = (data.author || '').trim()
  if (data.category !== undefined) b.category = (data.category || '').trim()
  if (data.version !== undefined) b.version = data.version || ''
  if (data.quality !== undefined) b.quality = data.quality || ''
  if (data.price !== undefined) b.price = Math.max(0, Number(data.price) || 0)
  if (data.remark !== undefined) b.remark = (data.remark || '').trim()
  saveBooks(list)
  return { ok: true }
}

function removeBook(isbn) {
  saveBooks(getBooks().filter(b => b.isbn !== trimIsbn(isbn)))
}

function myBooks(seller) {
  return getBooks().filter(b => b.seller === seller)
}

// ---------- 订单（买卖，暂不涉及付款） ----------
// 状态：0-待确认 1-已成交 2-已取消 3-已拒绝
function getOrders() {
  return wx.getStorageSync(ORDER_KEY) || []
}

function saveOrders(list) {
  wx.setStorageSync(ORDER_KEY, list)
}

function activeOrderFor(isbn) {
  return getOrders().find(o => o.isbn === trimIsbn(isbn) && o.status === 0) || null
}

function ordersFor(isbn) {
  return getOrders().filter(o => o.isbn === trimIsbn(isbn))
}

function myOrders(buyer) {
  return getOrders().filter(o => o.buyer === buyer)
}

function applyOrder(isbn, buyer) {
  const book = findBook(isbn)
  if (!book) return { ok: false, msg: '图书不存在' }
  if (book.status !== 0) return { ok: false, msg: '该书已售出或已下架' }
  if (book.seller === buyer) return { ok: false, msg: '不能购买自己发布的图书' }
  if (activeOrderFor(isbn)) return { ok: false, msg: '该书已有待确认订单' }
  const order = {
    id: genId(),
    isbn: book.isbn,
    title: book.title,
    price: book.price,
    buyer: buyer,
    seller: book.seller,
    status: 0,
    applyTime: nowStr(),
    doneTime: ''
  }
  const list = getOrders()
  list.unshift(order)
  saveOrders(list)
  return { ok: true, msg: '已下单，等待卖家确认', order }
}

function confirmOrder(id) {
  const list = getOrders()
  const order = list.find(o => o.id === id)
  if (!order || order.status !== 0) return { ok: false, msg: '订单不存在或非待确认状态' }
  order.status = 1
  order.doneTime = nowStr()
  saveOrders(list)
  setBookStatus(order.isbn, 1) // 成交后图书标记为已售出
  return { ok: true, msg: '已确认成交，图书已售出' }
}

function rejectOrder(id) {
  const list = getOrders()
  const order = list.find(o => o.id === id)
  if (!order || order.status !== 0) return { ok: false, msg: '订单不存在或非待确认状态' }
  order.status = 3
  order.doneTime = nowStr()
  saveOrders(list)
  return { ok: true, msg: '已拒绝该订单' }
}

function cancelOrder(id) {
  const list = getOrders()
  const order = list.find(o => o.id === id)
  if (!order || order.status !== 0) return { ok: false, msg: '订单不存在或非待确认状态' }
  order.status = 2
  order.doneTime = nowStr()
  saveOrders(list)
  return { ok: true, msg: '已取消订单' }
}

// ---------- 扫码历史 ----------
function getHistory() {
  return wx.getStorageSync(HISTORY_KEY) || []
}

// kind: 'scan' 仅识别 | 'shelf' 上架 | 'delete' 删除
function addHistory(isbn, title, kind) {
  const list = getHistory()
  list.unshift({
    isbn: trimIsbn(isbn),
    title: title || '未知书名',
    shelf: kind === 'shelf',
    time: nowStr()
  })
  wx.setStorageSync(HISTORY_KEY, list.slice(0, 50))
}

// 上架成功后，把该 ISBN 最近一条扫码记录标记为“已上架”，并回填书名（扫码时可能还是“未收录”）
function markShelf(isbn, title) {
  const i = trimIsbn(isbn)
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

// ---------- 登录 ----------
function isLogin() {
  return !!getLogin()
}

function getLogin() {
  return wx.getStorageSync(LOGIN_KEY) || null
}

function setLogin(role, name) {
  wx.setStorageSync(LOGIN_KEY, {
    role: role,
    name: (name || '').trim() || '同学',
    time: Date.now()
  })
}

function logout() {
  wx.removeStorageSync(LOGIN_KEY)
}

function isAdmin() {
  const l = getLogin()
  return !!l && l.role === 'admin'
}

function isUser() {
  const l = getLogin()
  return !!l && (l.role === 'user' || l.role === 'student')
}

function ownerName() {
  const l = getLogin()
  return (l && l.name) || '同学'
}

function timeStr(t) {
  return t || ''
}

module.exports = {
  getBooks,
  onSaleList,
  findBook,
  publishBook,
  setBookStatus,
  updateBook,
  removeBook,
  myBooks,
  getOrders,
  activeOrderFor,
  ordersFor,
  myOrders,
  applyOrder,
  confirmOrder,
  rejectOrder,
  cancelOrder,
  getHistory,
  addHistory,
  markShelf,
  getStats,
  isLogin,
  getLogin,
  setLogin,
  logout,
  isAdmin,
  isUser,
  ownerName,
  timeStr
}