import { ENV } from '@/api/env'
import { http } from '@/api/http'
import type { CreateOrderRequest, CreateOrderResponse, Order, OrderStatus } from '@/api/contracts'
import {
  mockCreateOrder,
  mockGetOrder,
  mockListOrders,
  mockPatchOrderStatus,
} from '@/api/mock'
import { emitOrderCreated } from '@/domain/orderEvents'

export async function createOrder(req: CreateOrderRequest) {
  const res = ENV.USE_MOCK
    ? await mockCreateOrder(req)
    : await http<CreateOrderResponse>('/orders', { method: 'POST', json: req })
  emitOrderCreated(res.id)
  return res
}

export function getOrder(orderId: string) {
  if (ENV.USE_MOCK) return mockGetOrder(orderId)
  return http<Order>(`/orders/${encodeURIComponent(orderId)}`)
}

export function listOrders(params: { status?: OrderStatus[] }, kitchenToken?: string) {
  if (ENV.USE_MOCK) return mockListOrders(params)

  const qs = new URLSearchParams()
  if (params.status && params.status.length > 0) {
    qs.set('status', params.status.join(','))
  }
  const suffix = qs.toString() ? `?${qs.toString()}` : ''
  return http<Order[]>(`/orders${suffix}`, { kitchenToken })
}

export function patchOrderStatus(
  orderId: string,
  newStatus: OrderStatus,
  kitchenToken?: string,
) {
  if (ENV.USE_MOCK) return mockPatchOrderStatus(orderId, newStatus)

  return http<Order>(`/orders/${encodeURIComponent(orderId)}/status`, {
    method: 'PATCH',
    json: { newStatus, status: newStatus },
    kitchenToken,
  })
}
