import request from './request'

export const pageLogs = params => request.get('/log/page', { params })
export const getLog = id => request.get(`/log/${id}`)
export const deleteLog = id => request.delete(`/log/delete/${id}`)
