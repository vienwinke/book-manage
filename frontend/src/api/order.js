import request from './request'

// 分页+状态筛选查询订单
export const pageOrders = params => request.get('/order/page', { params })
export const getOrder = id => request.get(`/order/${id}`)
// 下单购买（传 bookId，buyerId 后端取当前登录用户）
export const applyOrder = bookId => request.post('/order/apply', { bookId })
// 确认成交 / 拒绝 / 取消 / 删除
export const confirmOrder = (id, remark) => request.post(`/order/confirm/${id}`, { remark })
export const rejectOrder = (id, remark) => request.post(`/order/reject/${id}`, { remark })
export const cancelOrder = id => request.post(`/order/cancel/${id}`)
export const deleteOrder = id => request.delete(`/order/delete/${id}`)