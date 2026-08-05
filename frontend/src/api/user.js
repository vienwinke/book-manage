import request from './request'

export const pageUsers = params => request.get('/user/page', { params })
export const getUser = id => request.get(`/user/${id}`)
export const addUser = data => request.post('/user/add', data)
export const updateUser = data => request.put('/user/update', data)
export const deleteUser = id => request.delete(`/user/delete/${id}`)
