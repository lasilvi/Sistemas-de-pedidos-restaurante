const KEY = 'orders_mvp_kitchen_token_v1'

export function getKitchenToken(): string {
  try {
    return sessionStorage.getItem(KEY) ?? ''
  } catch {
    return ''
  }
}

export function setKitchenToken(token: string) {
  try {
    sessionStorage.setItem(KEY, token)
  } catch {
    // ignore
  }
}

export function clearKitchenToken() {
  try {
    sessionStorage.removeItem(KEY)
  } catch {
    // ignore
  }
}
