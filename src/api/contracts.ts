export type Product = {
  id: string
  name: string
  description?: string
  price?: number
  isActive?: boolean
}

export type OrderStatus =
  | 'PENDING'
  | 'IN_PREPARATION'
  | 'READY'

export type OrderItem = {
  productId: string
  quantity: number
  note?: string
  // info extra para UI (no necesariamente viene del backend)
  name?: string
}

export type Order = {
  id: string
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
  id: string
  status: OrderStatus
}
