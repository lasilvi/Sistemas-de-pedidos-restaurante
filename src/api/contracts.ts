export type Product = {
  id: string
  name: string
  description?: string
  price?: number
  isActive?: boolean
}

export type OrderStatus =
  | 'CREATED'
  | 'SUBMITTED'
  | 'IN_PREPARATION'
  | 'READY'
  | 'DELIVERED'
  | 'CANCELED'

export type OrderItem = {
  productId: string
  quantity: number
  note?: string
  // info extra para UI (no necesariamente viene del backend)
  name?: string
}

export type Order = {
  id: string
  orderId?: string // por compatibilidad si backend usa orderId en lugar de id
  tableId: number
  status: OrderStatus
  items: OrderItem[]
  note?: string
  createdAt?: string
  updatedAt?: string
  statusHistory?: Array<{ status: OrderStatus; changedAt: string; changedBy?: string }>
}

export type CreateOrderRequest = {
  tableId: number
  items: Array<{ productId: string; quantity: number; note?: string }>
  note?: string
}

export type CreateOrderResponse = {
  orderId: string
  status: OrderStatus
}
