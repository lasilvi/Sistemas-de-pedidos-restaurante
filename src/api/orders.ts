import type { CreateOrderRequest, CreateOrderResponse, Order, OrderStatus } from '@/api/contracts'
import {
  mockCreateOrder,
  mockGetOrder,
  mockListOrders,
  mockPatchOrderStatus,
} from '@/api/mock'

export function createOrder(req: CreateOrderRequest) {
  return mockCreateOrder(req)
}

export function getOrder(orderId: string) {
  return mockGetOrder(orderId)
}

export function listOrders(params: { status?: OrderStatus[] }, kitchenToken?: string) {
  void kitchenToken
  return mockListOrders(params)
}

export function patchOrderStatus(
  orderId: string,
  newStatus: OrderStatus,
  kitchenToken?: string,
) {
  void kitchenToken
  return mockPatchOrderStatus(orderId, newStatus)
}
