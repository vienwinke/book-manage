import request from './request'

// 分页+条件查询
export const pageBooks = params => request.get('/book/page', { params })
export const getBook = id => request.get(`/book/${id}`)
export const addBook = data => request.post('/book/add', data)
export const updateBook = data => request.put('/book/update', data)
export const deleteBook = id => request.delete(`/book/delete/${id}`)
