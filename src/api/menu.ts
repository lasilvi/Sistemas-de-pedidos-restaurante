import type { Product } from '@/api/contracts'
import { ENV } from '@/api/env'
import { http } from '@/api/http'
import { mockGetMenu } from '@/api/mock'

export function getMenu() {
  return ENV.USE_MOCK ? mockGetMenu() : http<Product[]>('/menu')
}
