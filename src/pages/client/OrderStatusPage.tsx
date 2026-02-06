import { useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getOrder } from '@/api/orders'
import { ErrorState } from '@/components/ErrorState'
import { Loading } from '@/components/Loading'
import { SectionTitle } from '@/components/SectionTitle'
import { STATUS_LABEL } from '@/domain/orderStatus'
import { Badge } from '@/components/Badge'

export function OrderStatusPage() {
  const { orderId: orderIdParam } = useParams()
  const navigate = useNavigate()

  const [orderIdInput, setOrderIdInput] = useState(orderIdParam ?? '')

  const orderId = useMemo(() => (orderIdParam ? orderIdParam : ''), [orderIdParam])

  const orderQ = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => getOrder(orderId),
    enabled: Boolean(orderId),
    refetchInterval: 5_000,
  })

  function go() {
    const id = orderIdInput.trim()
    if (!id) return
    navigate(`/client/status/${encodeURIComponent(id)}`)
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Estado del pedido"
        subtitle="Ingresa tu orderId para consultar el estado actual."
      />

      <div className="card p-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <input
            className="input"
            placeholder="orderId (UUID)"
            value={orderIdInput}
            onChange={(e) => setOrderIdInput(e.target.value)}
            onKeyDown={(e) => (e.key === 'Enter' ? go() : null)}
          />
          <button className="btn btn-primary cursor-pointer" onClick={go}>
            Consultar
          </button>
        </div>

        {!orderId ? (
          <div className="mt-6 text-sm text-slate-400">Aún no has consultado un pedido.</div>
        ) : orderQ.isLoading ? (
          <div className="mt-6">
            <Loading label="Consultando estado…" />
          </div>
        ) : orderQ.isError ? (
          <div className="mt-6">
            <ErrorState
              title="No pudimos consultar el pedido"
              detail={(orderQ.error as Error).message}
              onRetry={() => orderQ.refetch()}
            />
          </div>
        ) : (
          <div className="mt-6 space-y-3">
            <div className="text-sm text-slate-400">Pedido:</div>
            <div className="text-sm">
              <span className="text-slate-400">orderId:</span>{' '}
              <Badge>{orderId}</Badge>
            </div>
            <div className="text-sm">
              <span className="text-slate-400">Mesa:</span>{' '}
              <Badge>Mesa {orderQ.data?.tableId}</Badge>
            </div>
            <div className="text-sm">
              <span className="text-slate-400">Estado:</span>{' '}
              <Badge>{STATUS_LABEL[orderQ.data?.status ?? 'PENDING']}</Badge>
            </div>
            <div className="text-xs text-slate-500">
              Actualiza automáticamente cada 5s.
            </div>
          </div>
        )}

        <div className="mt-6">
          <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/table')}>
            Volver al inicio
          </button>
        </div>
      </div>
    </div>
  )
}
