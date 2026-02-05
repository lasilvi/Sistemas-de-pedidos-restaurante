import { http } from '@/api/http'
import type { CreateOrderRequest, CreateOrderResponse, Order, OrderStatus } from '@/api/contracts'

export function createOrder(req: CreateOrderRequest) {
  return http<CreateOrderResponse>('/orders', { method: 'POST', json: req })
}

export function getOrder(orderId: string) {
  return http<Order>(`/orders/${encodeURIComponent(orderId)}`)
}

export function listOrders(params: { status?: OrderStatus[] }, kitchenToken?: string) {
  const qs = new URLSearchParams()
  if (params.status && params.status.length > 0) {
    // compat: puede ser 'status=SUBMITTED' o múltiples 'status=...'
    // Implementación conservadora: si son muchos, manda como CSV; backend puede decidir.
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
  // Para maximizar compatibilidad con el backend, enviamos ambas claves:
  // - { newStatus: "..." } (recomendado por el doc de eventos)
  // - { status: "..." } (alternativa común)
  return http<Order>(`/orders/${encodeURIComponent(orderId)}/status`, {
    method: 'PATCH',
    json: { newStatus, status: newStatus },
    kitchenToken,
  })
}
