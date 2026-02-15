import { ENV } from '@/api/env'
import { http } from '@/api/http'
import type { CreateOrderRequest, CreateOrderResponse, Order, OrderStatus } from '@/api/contracts'
import {
  mockCreateOrder,
  mockGetOrder,
  mockListOrders,
  mockPatchOrderStatus,
} from '@/api/mock'

export async function createOrder(req: CreateOrderRequest) {
  if (ENV.USE_MOCK) return mockCreateOrder(req)

  try {
    return await http<CreateOrderResponse>('/orders', { method: 'POST', json: req })
  } catch (error) {
    console.warn('Falling back to mock createOrder:', error)
    return mockCreateOrder(req)
  }
}

export async function getOrder(orderId: string) {
  if (ENV.USE_MOCK) return mockGetOrder(orderId)

  try {
    return await http<Order>(`/orders/${encodeURIComponent(orderId)}`)
  } catch (error) {
    console.warn('Falling back to mock getOrder:', error)
    return mockGetOrder(orderId)
  }
}

export async function listOrders(params: { status?: OrderStatus[] }, kitchenToken?: string) {
  if (ENV.USE_MOCK) return mockListOrders(params)

  const qs = new URLSearchParams()
  if (params.status && params.status.length > 0) {
    qs.set('status', params.status.join(','))
  }
  const suffix = qs.toString() ? `?${qs.toString()}` : ''

  try {
    return await http<Order[]>(`/orders${suffix}`, { kitchenToken })
  } catch (error) {
    console.warn('Falling back to mock listOrders:', error)
    return mockListOrders(params)
  }
}

export async function patchOrderStatus(
  orderId: string,
  newStatus: OrderStatus,
  kitchenToken?: string,
) {
  if (ENV.USE_MOCK) return mockPatchOrderStatus(orderId, newStatus)

  try {
    return await http<Order>(`/orders/${encodeURIComponent(orderId)}/status`, {
      method: 'PATCH',
      json: { newStatus, status: newStatus },
      kitchenToken,
    })
  } catch (error) {
    console.warn('Falling back to mock patchOrderStatus:', error)
    return mockPatchOrderStatus(orderId, newStatus)
  }
}
