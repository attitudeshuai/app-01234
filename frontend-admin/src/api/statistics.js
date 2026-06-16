import request from '@/utils/request'

export function getDashboard() {
  return request.get('/statistics/dashboard')
}

export function getRoomStats(params) {
  return request.get('/statistics/rooms', { params })
}

export function getUsageStats(params) {
  return request.get('/statistics/usage', { params })
}

export function getRentalStats(params) {
  return request.get('/statistics/rental', { params })
}
