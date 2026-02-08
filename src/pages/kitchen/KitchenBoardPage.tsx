import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { listOrders, patchOrderStatus } from '@/api/orders'
import type { Order, OrderStatus } from '@/api/contracts'
import { ACTIVE_STATUSES, NEXT_STATUSES, STATUS_LABEL } from '@/domain/orderStatus'
import { onOrderCreated } from '@/domain/orderEvents'
import { SectionTitle } from '@/components/SectionTitle'
import { Badge } from '@/components/Badge'
import { ErrorState } from '@/components/ErrorState'
import { Loading } from '@/components/Loading'

export function KitchenBoardPage() {
  const navigate = useNavigate()

  const [statusFilter, setStatusFilter] = useState<OrderStatus[]>(ACTIVE_STATUSES)
  const [orders, setOrders] = useState<Order[]>([])
  const [initialLoading, setInitialLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState<string>('')
  const [patching, setPatching] = useState(false)

  const inFlightRef = useRef(false)
  const mountedRef = useRef(false)
  const hasLoadedRef = useRef(false)

  const loadOrders = useCallback(
    async ({ block }: { block: boolean }) => {
      if (inFlightRef.current) return
      inFlightRef.current = true
      if (block) setInitialLoading(true)
      else setRefreshing(true)

    try {
      const data = await listOrders({ status: statusFilter })
      if (!mountedRef.current) return
      setOrders(data)
      setError('')
      } catch (err) {
        if (!mountedRef.current) return
        const msg = err instanceof Error ? err.message : 'No pudimos cargar pedidos'
        setError(msg)
      } finally {
      if (!mountedRef.current) return
      inFlightRef.current = false
      hasLoadedRef.current = true
      if (block) setInitialLoading(false)
      else setRefreshing(false)
    }
  },
  [statusFilter],
)

  useEffect(() => {
    mountedRef.current = true
    return () => {
      mountedRef.current = false
      inFlightRef.current = false
    }
  }, [])

  useEffect(() => {
    loadOrders({ block: !hasLoadedRef.current })
  }, [loadOrders])

  useEffect(() => {
    return onOrderCreated(() => {
      loadOrders({ block: false })
    })
  }, [loadOrders])

  const grouped = useMemo(() => {
    const by: Record<OrderStatus, Order[]> = {
      PENDING: [],
      IN_PREPARATION: [],
      READY: [],
    }
    for (const o of orders) by[o.status]?.push(o)
    return by
  }, [orders])

  if (initialLoading) return <Loading label="Cargando pedidos..." />

  if (error && orders.length === 0) {
    return (
      <ErrorState
        title="No pudimos cargar pedidos"
        detail={error}
        onRetry={() => loadOrders({ block: true })}
      />
    )
  }

  function orderIdOf(o: Order) {
    return o.id
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Bandeja de cocina"
        subtitle={`Pedidos activos (actualiza al recibir nuevos pedidos).${refreshing ? ' Actualizando...' : ''}`}
        right={
          <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/table')}>
            Ir a cliente
          </button>
        }
      />

      {error && orders.length > 0 ? (
        <div className="card flex flex-wrap items-center justify-between gap-3 p-4 text-sm text-slate-200">
          <div>
            <div className="font-semibold">No pudimos actualizar pedidos</div>
            <div className="text-xs text-slate-400">{error}</div>
          </div>
          <button className="btn btn-ghost cursor-pointer" onClick={() => loadOrders({ block: false })}>
            Reintentar
          </button>
        </div>
      ) : null}

      <div className="card p-4">
        <div className="flex flex-wrap items-center gap-2">
          <div className="text-xs text-slate-400">Filtrar:</div>
          {ACTIVE_STATUSES.map((s) => (
            <button
              key={s}
              className={`btn cursor-pointer ${statusFilter.includes(s) ? 'btn-primary' : 'btn-ghost'}`}
              onClick={() =>
                setStatusFilter((prev) =>
                  prev.includes(s) ? prev.filter((x) => x !== s) : [...prev, s],
                )
              }
            >
              {STATUS_LABEL[s]}
            </button>
          ))}
          <button
            className="btn btn-ghost cursor-pointer"
            onClick={() => setStatusFilter(ACTIVE_STATUSES)}
          >
            Reset
          </button>
          <button
            className="btn btn-ghost cursor-pointer"
            onClick={() => loadOrders({ block: false })}
            disabled={refreshing}
          >
            Actualizar
          </button>
        </div>
      </div>

      {orders.length === 0 ? (
        <div className="card p-6 text-sm text-slate-400">No hay pedidos con el filtro actual.</div>
      ) : (
        <div className="space-y-4">
          {ACTIVE_STATUSES.map((status) =>
            grouped[status].length === 0 ? null : (
              <div key={status} className="card p-6">
                <div className="mb-4 flex items-center justify-between">
                  <div className="text-base font-semibold">{STATUS_LABEL[status]}</div>
                  <Badge>{grouped[status].length}</Badge>
                </div>

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
                        } finally {
                          setPatching(false)
                        }
                      }}
                    />
                  ))}
                </div>
              </div>
            ),
          )}
        </div>
      )}

      <div className="text-xs text-slate-500">
        Si tu backend necesita un query diferente para status (CSV vs repeated params), ajusta `src/api/orders.ts`.
      </div>
    </div>
  )
}

function OrderRow({
  order,
  patchPending,
  onChangeStatus,
}: {
  order: Order
  patchPending: boolean
  onChangeStatus: (s: OrderStatus) => void
}) {
  const id = order.id
  const next = NEXT_STATUSES[order.status] ?? []

  return (
    <div className="rounded-2xl bg-slate-950/40 p-4 ring-1 ring-slate-800">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="text-sm font-semibold">
            Mesa {order.tableId} · <span className="text-slate-400">#{id.slice(0, 8)}…</span>
          </div>
          <div className="mt-1 text-xs text-slate-500">
            Ítems: {order.items?.reduce((acc, i) => acc + (i.quantity ?? 0), 0) ?? 0}
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {next.map((s) => (
            <button
              key={s}
              className="btn btn-primary cursor-pointer"
              disabled={patchPending}
              onClick={() => onChangeStatus(s)}
            >
              Pasar a {STATUS_LABEL[s]}
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
