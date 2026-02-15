import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ChefHat, Clock, LogOut } from 'lucide-react'
import { motion } from 'motion/react'
import { getMenu } from '@/api/menu'
import { listOrders, patchOrderStatus } from '@/api/orders'
import { HttpError } from '@/api/http'
import type { Order, OrderStatus } from '@/api/contracts'
import { ACTIVE_STATUSES, NEXT_STATUSES, STATUS_LABEL } from '@/domain/orderStatus'
import { clearKitchenToken, getKitchenToken } from '@/store/kitchenAuth'
import { SectionTitle } from '@/components/SectionTitle'
import { Badge } from '@/components/Badge'
import { ErrorState } from '@/components/ErrorState'
import { Loading } from '@/components/Loading'
import { ErrorState } from '@/components/ErrorState'
import { Card } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Dialog, DialogSection } from '@/components/ui/dialog'
import { useToast } from '@/components/ui/toast'

const COLUMNS: Array<{ status: OrderStatus; title: string; tone: 'warning' | 'success' | 'outline' }> = [
  { status: 'PENDING', title: 'Pendiente', tone: 'warning' },
  { status: 'IN_PREPARATION', title: 'En preparacion', tone: 'outline' },
  { status: 'READY', title: 'Listo', tone: 'success' },
]

const POLL_MS = 3000

function formatDateTime(value?: string) {
  if (!value) return 'N/A'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('es-CO')
}

export function KitchenBoardPage() {
  const navigate = useNavigate()
  const { toast } = useToast()

  const {
    kitchenDetailOrderId,
    openKitchenOrderDetail,
    closeKitchenOrderDetail,
  } = useApp()

  const token = getKitchenToken()
  const menuQ = useQuery({ queryKey: ['menu'], queryFn: getMenu, staleTime: 60_000 })

  const [orders, setOrders] = useState<Order[]>([])
  const [initialLoading, setInitialLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')
  const [patchingOrderId, setPatchingOrderId] = useState('')

  const timeoutRef = useRef<number | null>(null)
  const mountedRef = useRef(false)
  const inFlightRef = useRef(false)

  const loadOrders = useCallback(
    async ({ block }: { block: boolean }) => {
      if (inFlightRef.current) return
      inFlightRef.current = true

      if (block) setInitialLoading(true)
      else setRefreshing(true)

      try {
        const kitchenToken = getKitchenToken()
        if (!kitchenToken) {
          navigate('/kitchen', { replace: true })
          return
        }
        const data = await listOrders({ status: statusFilter })
        if (!mountedRef.current) return
        setOrders(data)
        setError('')
      } catch (err) {
        if (err instanceof HttpError && err.status === 401) {
          clearKitchenToken()
          navigate('/kitchen', { replace: true })
          return
        }
        if (!mountedRef.current) return
        setError(err instanceof Error ? err.message : 'No pudimos cargar pedidos')
      } finally {
        inFlightRef.current = false
        if (mountedRef.current) {
          if (block) setInitialLoading(false)
          else setRefreshing(false)

          timeoutRef.current = window.setTimeout(() => {
            if (mountedRef.current) loadOrders({ block: false })
          }, POLL_MS)
        }
      }
    },
    [navigate, statusFilter],
  )

  useEffect(() => {
    if (!token) {
      navigate('/kitchen', { replace: true })
      return
    }

    mountedRef.current = true
    loadOrders({ block: true })

    return () => {
      mountedRef.current = false
      inFlightRef.current = false
      if (timeoutRef.current) window.clearTimeout(timeoutRef.current)
    }
  }, [loadOrders, navigate, token])

  const grouped = useMemo(() => {
    const by: Record<OrderStatus, Order[]> = {
      PENDING: [],
      IN_PREPARATION: [],
      READY: [],
    }
    for (const order of orders) by[order.status]?.push(order)
    return by
  }, [orders])

  const productNames = useMemo(() => buildProductNameMap(menuQ.data), [menuQ.data])

  const selectedOrder = useMemo(
    () => orders.find((order) => order.id === kitchenDetailOrderId) ?? null,
    [kitchenDetailOrderId, orders],
  )

  useEffect(() => {
    if (!kitchenDetailOrderId) return
    if (!selectedOrder) closeKitchenOrderDetail()
  }, [closeKitchenOrderDetail, kitchenDetailOrderId, selectedOrder])

  async function move(order: Order, status: OrderStatus) {
    try {
      setPatchingOrderId(order.id)
      await patchOrderStatus(order.id, status, token)
      toast({
        title: 'Pedido actualizado',
        description: `Estado: ${STATUS_LABEL[status]}`,
        tone: 'success',
      })
      await loadOrders({ block: false })
    } catch (err) {
      toast({
        title: 'No se pudo actualizar',
        description: err instanceof Error ? err.message : 'Error inesperado',
        tone: 'danger',
      })
    } finally {
      setPatchingOrderId('')
    }
  }

  function logout() {
    clearKitchenToken()
    navigate('/kitchen', { replace: true })
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Bandeja de cocina"
        subtitle={`Pedidos activos (refresca cada 3s).${refreshing ? ' Actualizando...' : ''}`}
        right={
          <div className="flex items-center gap-2">
            <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/table')}>
              Ir a cliente
            </button>
            <button
              className="btn btn-ghost cursor-pointer"
              onClick={() => {
                clearKitchenToken()
                navigate('/kitchen', { replace: true })
              }}
            >
              Cerrar sesion
            </button>
          </div>
        }
      />

  if (error && orders.length === 0) {
    return <ErrorState title="No pudimos cargar pedidos" detail={error} onRetry={() => loadOrders({ block: true })} />
  }

                <div className="space-y-3">
                  {grouped[status].map((o) => (
                    <OrderRow
                      key={orderIdOf(o)}
                      order={o}
                      patchPending={patching}
                      onChangeStatus={async (newStatus) => {
                        try {
                          setPatching(true)
                          await patchOrderStatus(orderIdOf(o), newStatus)
                          const data = await listOrders({ status: statusFilter })
                          setOrders(data)
                        } catch (err) {
                          if (err instanceof HttpError && err.status === 401) {
                            clearKitchenToken()
                            navigate('/kitchen', { replace: true })
                            return
                          }
                          throw err
                        } finally {
                          setPatching(false)
                        }
                      }}
                    />
                  ))}
                </div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <ThemeToggle />
              <Link to="/client/table">
                <Button variant="outline" size="sm">Cliente</Button>
              </Link>
              <Button variant="ghost" size="icon" onClick={logout}>
                <LogOut className="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>
      </header>

      <main className="page-wrap py-6">
        {error ? (
          <Card className="mb-4 border-warning/30 bg-warning/10 p-3 text-sm text-muted-foreground">{error}</Card>
        ) : null}

        {activeOrders.length === 0 ? (
          <Card className="p-8 text-center text-sm text-muted-foreground">No hay pedidos activos.</Card>
        ) : (
          <div className="grid gap-6 xl:grid-cols-3">
            {COLUMNS.map((column) => {
              const columnOrders = grouped[column.status]
              return (
                <div key={column.status} className="space-y-3">
                  <div className="flex items-center gap-2">
                    <h2 className="text-lg font-semibold">{column.title}</h2>
                    <Badge variant={column.tone}>{columnOrders.length}</Badge>
                  </div>

                  <div className="space-y-3">
                    {columnOrders.map((order, index) => {
                      const totalItems = order.items.reduce((sum, item) => sum + item.quantity, 0)
                      const next = NEXT_STATUS[order.status]
                      const previous = PREVIOUS_STATUS[order.status]
                      const ageMinutes = order.createdAt
                        ? Math.max(0, Math.floor((Date.now() - new Date(order.createdAt).getTime()) / 60000))
                        : null

                      return (
                        <motion.div
                          key={order.id}
                          initial={{ opacity: 0, y: 10 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ delay: index * 0.03, duration: 0.2 }}
                        >
                          <Card
                            className="cursor-pointer p-4 transition hover:border-accent/40 hover:shadow-md"
                            onClick={() => openKitchenOrderDetail(order)}
                          >
                            <div className="mb-3 flex items-start justify-between">
                              <div>
                                <h3 className="text-lg font-semibold">Mesa {order.tableId}</h3>
                                <p className="text-xs text-muted-foreground">#{order.id.slice(0, 8)}</p>
                              </div>
                              <div className="flex items-center gap-2">
                                <Badge variant="outline">{totalItems} items</Badge>
                                {ageMinutes !== null ? (
                                  <span className="flex items-center gap-1 text-xs text-muted-foreground">
                                    <Clock className="h-3 w-3" />
                                    {ageMinutes}m
                                  </span>
                                ) : null}
                              </div>
                            </div>

                            <div className="mb-3 space-y-1 text-sm">
                              {order.items.slice(0, 3).map((item) => (
                                <div key={`${order.id}-${item.productId}-${item.note ?? ''}`} className="flex items-center justify-between">
                                  <span>{resolveOrderItemName(item, productNames)}</span>
                                  <span>x{item.quantity}</span>
                                </div>
                              ))}
                              {order.items.length > 3 ? (
                                <p className="text-xs text-muted-foreground">+ {order.items.length - 3} mas</p>
                              ) : null}
                              {order.note ? <p className="text-xs text-muted-foreground">Nota: {order.note}</p> : null}
                            </div>

                            <div className="flex flex-wrap gap-2">
                              {previous ? (
                                <Button
                                  variant="outline"
                                  size="sm"
                                  disabled={patchingOrderId === order.id}
                                  onClick={(event) => {
                                    event.stopPropagation()
                                    move(order, previous)
                                  }}
                                >
                                  Volver
                                </Button>
                              ) : null}
                              {next ? (
                                <Button
                                  size="sm"
                                  disabled={patchingOrderId === order.id}
                                  onClick={(event) => {
                                    event.stopPropagation()
                                    move(order, next)
                                  }}
                                >
                                  {next === 'IN_PREPARATION' ? 'Iniciar' : 'Marcar listo'}
                                </Button>
                              ) : null}
                            </div>
                          </Card>
                        </motion.div>
                      )
                    })}
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </main>

      <Dialog
        open={Boolean(selectedOrder)}
        onOpenChange={(open) => {
          if (!open) closeKitchenOrderDetail()
        }}
        title={selectedOrder ? `Mesa ${selectedOrder.tableId} · ${STATUS_LABEL[selectedOrder.status]}` : 'Pedido'}
        contentClassName="max-w-3xl"
      >
        {selectedOrder ? (
          <div className="space-y-4 text-sm">
            <DialogSection>
              <div className="grid gap-2 rounded-xl border border-border p-3">
                <p><span className="font-medium">ID completo:</span> {selectedOrder.id}</p>
                <p><span className="font-medium">Mesa:</span> {selectedOrder.tableId}</p>
                <p><span className="font-medium">Estado:</span> {STATUS_LABEL[selectedOrder.status]}</p>
                <p><span className="font-medium">Creado:</span> {formatDateTime(selectedOrder.createdAt)}</p>
                <p><span className="font-medium">Actualizado:</span> {formatDateTime(selectedOrder.updatedAt)}</p>
              </div>
            </DialogSection>

            <DialogSection>
              <h4 className="font-medium">Items del pedido</h4>
              <div className="space-y-2">
                {selectedOrder.items.map((item) => (
                  <div
                    key={`${selectedOrder.id}-${item.productId}-${item.note ?? ''}`}
                    className="rounded-xl border border-border p-3"
                  >
                    <div className="flex items-center justify-between">
                      <span>{resolveOrderItemName(item, productNames)}</span>
                      <span className="font-medium">x{item.quantity}</span>
                    </div>
                    {item.note ? <p className="mt-1 text-xs text-muted-foreground">Nota: {item.note}</p> : null}
                  </div>
                ))}
              </div>
            </DialogSection>

            {selectedOrder.note ? (
              <DialogSection>
                <h4 className="font-medium">Nota general</h4>
                <p className="rounded-xl border border-border p-3 text-muted-foreground">{selectedOrder.note}</p>
              </DialogSection>
            ) : null}
          </div>
        ) : null}
      </Dialog>
    </div>
  )
}
