export type Product = {
  id: number
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
  productId: number
  quantity: number
  note?: string
  // info extra para UI (viene del backend)
  productName?: string
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
  items: Array<{ productId: number; quantity: number; note?: string }>
  note?: string
}

export type CreateOrderResponse = {
  id: string
  status: OrderStatus
}
