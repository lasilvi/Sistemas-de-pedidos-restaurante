import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { listOrders, patchOrderStatus } from '@/api/orders'
import type { Order, OrderStatus } from '@/api/contracts'
import { ACTIVE_STATUSES, NEXT_STATUSES, STATUS_LABEL } from '@/domain/orderStatus'
import { SectionTitle } from '@/components/SectionTitle'
import { Badge } from '@/components/Badge'
import { ErrorState } from '@/components/ErrorState'
import { Loading } from '@/components/Loading'
import { clearKitchenToken, getKitchenToken } from '@/store/kitchenAuth'

export function KitchenBoardPage() {
  const navigate = useNavigate()
  const kitchenToken = getKitchenToken()

  const [statusFilter, setStatusFilter] = useState<OrderStatus[]>(ACTIVE_STATUSES)

  const ordersQ = useQuery({
    queryKey: ['kitchen-orders', statusFilter.join(',')],
    queryFn: () => listOrders({ status: statusFilter }, kitchenToken),
    enabled: Boolean(kitchenToken),
    refetchInterval: 3_000,
  })

  const patchM = useMutation({
    mutationFn: (p: { orderId: string; newStatus: OrderStatus }) =>
      patchOrderStatus(p.orderId, p.newStatus, kitchenToken),
    onSuccess: () => ordersQ.refetch(),
  })

  if (!kitchenToken) {
    return (
      <ErrorState
        title="No has iniciado sesión de cocina"
        detail="Ingresa el PIN para continuar."
        onRetry={() => navigate('/kitchen')}
      />
    )
  }

  if (ordersQ.isLoading) return <Loading label="Cargando pedidos…" />

  if (ordersQ.isError) {
    return (
      <ErrorState
        title="No pudimos cargar pedidos"
        detail={(ordersQ.error as Error).message}
        onRetry={() => ordersQ.refetch()}
      />
    )
  }

  const orders = ordersQ.data ?? []

  const grouped = useMemo(() => {
    const by: Record<OrderStatus, Order[]> = {
      CREATED: [],
      SUBMITTED: [],
      IN_PREPARATION: [],
      READY: [],
      DELIVERED: [],
      CANCELED: [],
    }
    for (const o of orders) by[o.status]?.push(o)
    return by
  }, [orders])

  function logout() {
    clearKitchenToken()
    navigate('/kitchen', { replace: true })
  }

  function orderIdOf(o: Order) {
    return o.orderId ?? o.id
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Bandeja de cocina"
        subtitle="Pedidos activos (refresca cada 3s)."
        right={
          <button className="btn btn-ghost cursor-pointer" onClick={logout}>
            Salir
          </button>
        }
      />

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
                      patchPending={patchM.isPending}
                      onChangeStatus={(newStatus) =>
                        patchM.mutate({ orderId: orderIdOf(o), newStatus })
                      }
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
  const id = order.orderId ?? order.id
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
