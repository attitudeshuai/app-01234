import request from '@/utils/request'

export function getRoomList(params) {
  return request.get('/rooms', { params })
}

export function getRoomsByBuilding(buildingId) {
  return request.get(`/rooms/by-building/${buildingId}`)
}

export function createRoom(data) {
  return request.post('/rooms', data)
}

export function updateRoom(id, data) {
  return request.put(`/rooms/${id}`, data)
}

export function updateRoomStatus(id, status) {
  return request.put(`/rooms/${id}/status`, { status })
}

export function deleteRoom(id) {
  return request.delete(`/rooms/${id}`)
}
