import { http } from '@/api/http'
import type { Product } from '@/api/contracts'

export function getMenu() {
  return http<Product[]>('/menu')
}
