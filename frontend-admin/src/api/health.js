import request from '@/utils/request'

export function getHealthList(params) {
  return request.get('/room-health', { params })
}

export function createHealth(data) {
  return request.post('/room-health', data)
}

export function updateHealth(id, data) {
  return request.put(`/room-health/${id}`, data)
}
