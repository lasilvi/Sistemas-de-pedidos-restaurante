type OrderEvent = {
  type: 'order-created'
  orderId?: string
  at: number
}

const CHANNEL_NAME = 'restaurant-order-events'
const STORAGE_KEY = `${CHANNEL_NAME}:order-created`

function safeParse(raw: string | null): OrderEvent | null {
  if (!raw) return null
  try {
    return JSON.parse(raw) as OrderEvent
  } catch {
    return null
  }
}

export function emitOrderCreated(orderId?: string) {
  if (typeof window === 'undefined') return
  const payload: OrderEvent = { type: 'order-created', orderId, at: Date.now() }

  try {
    const channel = new BroadcastChannel(CHANNEL_NAME)
    channel.postMessage(payload)
    channel.close()
  } catch {
    // Ignore if BroadcastChannel is not available.
  }

  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
  } catch {
    // Ignore storage errors (private mode, quota, etc).
  }
}

export function onOrderCreated(callback: (event: OrderEvent) => void) {
  if (typeof window === 'undefined') return () => {}

  let channel: BroadcastChannel | null = null
  const handler = (event: OrderEvent | null) => {
    if (!event || event.type !== 'order-created') return
    callback(event)
  }

  try {
    channel = new BroadcastChannel(CHANNEL_NAME)
    channel.onmessage = (evt) => handler(evt.data as OrderEvent)
  } catch {
    // BroadcastChannel not supported.
  }

  const onStorage = (evt: StorageEvent) => {
    if (evt.key !== STORAGE_KEY) return
    handler(safeParse(evt.newValue))
  }

  window.addEventListener('storage', onStorage)

  return () => {
    if (channel) channel.close()
    window.removeEventListener('storage', onStorage)
  }
}
