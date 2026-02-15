import type {
  CreateOrderRequest,
  CreateOrderResponse,
  Order,
  OrderStatus,
  Product,
} from '@/api/contracts'

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
    id: 1,
    name: 'Empanadas criollas',
    description: 'Empanadas de carne con salsa casera.',
    price: 450,
    isActive: true,
    category: 'entradas',
    imageUrl: 'https://images.unsplash.com/photo-1603360946369-dc9bb6258143?w=400&h=300&fit=crop',
  },
  {
    id: 2,
    name: 'Provoleta grillada',
    description: 'Queso provolone con oregano y oliva.',
    price: 520,
    isActive: true,
    category: 'entradas',
    imageUrl: 'https://picsum.photos/seed/provoleta-grillada/400/300',
  },
  {
    id: 3,
    name: 'Bife de chorizo',
    description: 'Corte premium con papas rusticas.',
    price: 1850,
    isActive: true,
    category: 'principales',
    imageUrl: 'https://images.unsplash.com/photo-1558030006-450675393462?w=400&h=300&fit=crop',
  },
  {
    id: 4,
    name: 'Milanesa napolitana',
    description: 'Milanesa con salsa pomodoro y queso.',
    price: 1420,
    isActive: true,
    category: 'principales',
    imageUrl: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop',
  },
  {
    id: 5,
    name: 'Volcan de chocolate',
    description: 'Bizcocho tibio con centro fundido.',
    price: 480,
    isActive: true,
    category: 'postres',
    imageUrl: 'https://images.unsplash.com/photo-1624353365286-3f8d62daad51?w=400&h=300&fit=crop',
  },
  {
    id: 6,
    name: 'Limonada de la casa',
    description: 'Limon, menta y almibar ligero.',
    price: 280,
    isActive: true,
    category: 'bebidas',
    imageUrl: 'https://picsum.photos/seed/limonada-casa/400/300',
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
      items: [{ productId: 1, quantity: 2, note: 'Sin cebolla' }],
      note: 'Enviar cubiertos extra',
      createdAt,
      updatedAt: createdAt,
    },
    {
      id: nextId(),
      tableId: 5,
      status: 'IN_PREPARATION',
      items: [{ productId: 3, quantity: 1 }],
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
