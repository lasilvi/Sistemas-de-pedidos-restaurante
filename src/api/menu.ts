import type { Product } from '@/api/contracts'
import { mockGetMenu } from '@/api/mock'

export function getMenu() {
  return mockGetMenu()
}
