import request from '@/utils/request'

export function getRentalList(params) {
  return request.get('/rentals', { params })
}

export function createRental(data) {
  return request.post('/rentals', data)
}

export function updateRental(id, data) {
  return request.put(`/rentals/${id}`, data)
}

export function terminateRental(id) {
  return request.put(`/rentals/${id}/terminate`)
}

export function getContracts(rentalId) {
  return request.get(`/rentals/${rentalId}/contracts`)
}

export function getAllContracts() {
  return request.get('/rentals/contracts')
}

export function createContract(rentalId, data) {
  return request.post(`/rentals/${rentalId}/contracts`, data)
}
