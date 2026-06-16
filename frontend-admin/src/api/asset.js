import request from '@/utils/request'

export function getAssetList(params) {
  return request.get('/room-assets', { params })
}

export function createAsset(data) {
  return request.post('/room-assets', data)
}

export function updateAsset(id, data) {
  return request.put(`/room-assets/${id}`, data)
}

export function deleteAsset(id) {
  return request.delete(`/room-assets/${id}`)
}
