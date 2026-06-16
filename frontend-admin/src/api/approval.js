import request from '@/utils/request'

export function getApprovalList(params) {
  return request.get('/approvals', { params })
}

export function createApproval(data) {
  return request.post('/approvals', data)
}

export function approveApproval(id, data) {
  return request.put(`/approvals/${id}/approve`, data)
}

export function rejectApproval(id, data) {
  return request.put(`/approvals/${id}/reject`, data)
}
