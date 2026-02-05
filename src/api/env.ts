function required(name: string, fallback?: string): string {
  const v = import.meta.env[name] as string | undefined
  if (v && v.trim().length > 0) return v
  if (fallback) return fallback
  return ''
}

export const ENV = {
  API_BASE_URL: required('VITE_API_BASE_URL', 'http://localhost:8081'),
  KITCHEN_TOKEN_HEADER: required('VITE_KITCHEN_TOKEN_HEADER', 'X-Kitchen-Token'),
  KITCHEN_PIN: required('VITE_KITCHEN_PIN', '1234'),
  KITCHEN_FIXED_TOKEN: (import.meta.env.VITE_KITCHEN_FIXED_TOKEN as string | undefined) ?? '',
} as const
