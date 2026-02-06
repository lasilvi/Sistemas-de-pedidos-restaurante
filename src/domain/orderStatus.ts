import type { OrderStatus } from '@/api/contracts'

export const ACTIVE_STATUSES: OrderStatus[] = ['PENDING', 'IN_PREPARATION', 'READY']

export const STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: 'Pendiente',
  IN_PREPARATION: 'En preparación',
  READY: 'Listo',
}

export const NEXT_STATUSES: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ['IN_PREPARATION'],
  IN_PREPARATION: ['READY'],
  READY: [],
}

export function canTransition(from: OrderStatus, to: OrderStatus): boolean {
  return NEXT_STATUSES[from].includes(to)
}
