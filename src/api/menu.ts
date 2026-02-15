import type { Product } from '@/api/contracts'
import { ENV } from '@/api/env'
import { http } from '@/api/http'
import { mockGetMenu } from '@/api/mock'

export async function getMenu() {
  if (ENV.USE_MOCK) return mockGetMenu()

  try {
    return await http<Product[]>('/menu')
  } catch (error) {
    console.warn('Falling back to mock menu data:', error)
    return mockGetMenu()
  }
}
