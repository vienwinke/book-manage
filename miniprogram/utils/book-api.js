function httpGet(url) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: url,
      method: 'GET',
      timeout: 8000,
      success: res => resolve(res.data),
      fail: reject
    })
  })
}

function https(url) {
  return (url || '').replace(/^http:/, 'https:')
}

function fromGoogle(isbn) {
  return httpGet('https://www.googleapis.com/books/v1/volumes?q=isbn:' + isbn)
    .then(data => {
      const v = data && data.items && data.items[0] && data.items[0].volumeInfo
      if (!v || !v.title) return null
      const img = v.imageLinks && (v.imageLinks.thumbnail || v.imageLinks.smallThumbnail)
      return {
        title: v.title || '',
        author: (v.authors || []).join('、') || '',
        category: (v.categories && v.categories[0]) || '',
        publisher: v.publisher || '',
        publishedDate: v.publishedDate || '',
        cover: https(img),
        remark: ''
      }
    })
    .catch(() => null)
}

function fromOpenLibrary(isbn) {
  return httpGet('https://openlibrary.org/api/books?bibkeys=ISBN:' + isbn + '&format=json&jscmd=data')
    .then(data => {
      const item = data && data['ISBN:' + isbn]
      if (!item || !item.title) return null
      const img = item.cover && (item.cover.large || item.cover.medium || item.cover.small)
      return {
        title: item.title || '',
        author: (item.authors || []).map(a => a.name).join('、') || '',
        category: (item.subjects && item.subjects[0] && item.subjects[0].name) || '',
        publisher: (item.publishers || [])[0] || '',
        publishedDate: item.publish_date || '',
        cover: https(img),
        remark: ''
      }
    })
    .catch(() => null)
}

/**
 * 按 ISBN 云端识别书籍信息（先 Google Books，失败回退 OpenLibrary）
 * 返回 { title, author, category, publisher, publishedDate, cover } 或 null
 */
function fetchByIsbn(isbn) {
  return fromGoogle(isbn).then(info => info || fromOpenLibrary(isbn))
}

module.exports = { fetchByIsbn }
