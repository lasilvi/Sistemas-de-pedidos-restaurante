import type { CreateOrderRequest, CreateOrderResponse, Order, OrderStatus, Product } from '@/api/contracts'

const nowIso = () => new Date().toISOString()

let seq = 1
function nextId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  const id = `mock-${seq}`
  seq += 1
  return id
}

const products: Product[] = [
  {
    id: '1',
    name: 'Pizza Margherita',
    description: 'Pizza clasica con tomate, mozzarella y albahaca',
    isActive: true,
  },
  {
    id: '2',
    name: 'Hamburguesa Clasica',
    description: 'Hamburguesa de carne con lechuga, tomate y queso',
    isActive: true,
  },
  {
    id: '3',
    name: 'Ensalada Cesar',
    description: 'Ensalada fresca con pollo, parmesano y aderezo Cesar',
    isActive: true,
  },
]

const orders: Order[] = []
let seeded = false

function seedOrders() {
  if (seeded) return
  seeded = true

  const createdAt = nowIso()
  orders.push(
    {
      id: nextId(),
      tableId: 3,
      status: 'PENDING',
      items: [{ productId: '1', quantity: 2 }],
      createdAt,
      updatedAt: createdAt,
    },
    {
      id: nextId(),
      tableId: 5,
      status: 'IN_PREPARATION',
      items: [{ productId: '2', quantity: 1 }],
      createdAt,
      updatedAt: createdAt,
    },
  )
}

function findOrder(orderId: string) {
  return orders.find((o) => o.id === orderId)
}

export async function mockGetMenu(): Promise<Product[]> {
  seedOrders()
  return products.filter((p) => p.isActive)
}

export async function mockCreateOrder(req: CreateOrderRequest): Promise<CreateOrderResponse> {
  seedOrders()
  const id = nextId()
  const createdAt = nowIso()

  const order: Order = {
    id,
    tableId: req.tableId,
    status: 'PENDING',
    items: req.items.map((i) => ({
      productId: i.productId,
      quantity: i.quantity,
      note: i.note,
    })),
    note: req.note,
    createdAt,
    updatedAt: createdAt,
  }

  orders.unshift(order)

  return { id, status: order.status }
}

export async function mockGetOrder(orderId: string): Promise<Order> {
  seedOrders()
  const order = findOrder(orderId)
  if (!order) throw new Error('Pedido no encontrado')
  return order
}

export async function mockListOrders(params: { status?: OrderStatus[] }): Promise<Order[]> {
  seedOrders()
  const { status } = params
  if (!status || status.length === 0) return orders
  return orders.filter((o) => status.includes(o.status))
}

export async function mockPatchOrderStatus(orderId: string, newStatus: OrderStatus): Promise<Order> {
  seedOrders()
  const order = findOrder(orderId)
  if (!order) throw new Error('Pedido no encontrado')

  order.status = newStatus
  order.updatedAt = nowIso()

  return order
}
