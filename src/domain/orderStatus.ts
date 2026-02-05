import type { OrderStatus } from '@/api/contracts'

export const ACTIVE_STATUSES: OrderStatus[] = ['SUBMITTED', 'IN_PREPARATION', 'READY']

export const STATUS_LABEL: Record<OrderStatus, string> = {
  CREATED: 'Creado',
  SUBMITTED: 'Enviado',
  IN_PREPARATION: 'En preparación',
  READY: 'Listo',
  DELIVERED: 'Entregado',
  CANCELED: 'Cancelado',
}

export const NEXT_STATUSES: Record<OrderStatus, OrderStatus[]> = {
  CREATED: ['SUBMITTED'],
  SUBMITTED: ['IN_PREPARATION', 'CANCELED'],
  IN_PREPARATION: ['READY', 'CANCELED'],
  READY: ['DELIVERED'],
  DELIVERED: [],
  CANCELED: [],
}

export function canTransition(from: OrderStatus, to: OrderStatus): boolean {
  return NEXT_STATUSES[from].includes(to)
}
