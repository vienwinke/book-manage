import request from './request'

export const pageBorrows = params => request.get('/borrow/page', { params })
export const getBorrow = id => request.get(`/borrow/${id}`)
export const applyBorrow = data => request.post('/borrow/apply', data)
export const approveBorrow = (id, adminRemark) => request.post(`/borrow/approve/${id}`, { adminRemark })
export const rejectBorrow = (id, adminRemark) => request.post(`/borrow/reject/${id}`, { adminRemark })
export const returnBorrow = id => request.post(`/borrow/return/${id}`)
export const deleteBorrow = id => request.delete(`/borrow/delete/${id}`)
