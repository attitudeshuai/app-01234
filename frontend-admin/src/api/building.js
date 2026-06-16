import request from '@/utils/request'

export function getBuildingList(params) {
  return request.get('/buildings', { params })
}

export function getAllBuildings() {
  return request.get('/buildings/all')
}

export function getDepartments() {
  return request.get('/buildings/departments')
}

export function createBuilding(data) {
  return request.post('/buildings', data)
}

export function updateBuilding(id, data) {
  return request.put(`/buildings/${id}`, data)
}

export function updateBuildingStatus(id, status) {
  return request.put(`/buildings/${id}/status`, { status })
}

export function deleteBuilding(id) {
  return request.delete(`/buildings/${id}`)
}
