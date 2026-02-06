import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { createOrder } from '@/api/orders'
import { SectionTitle } from '@/components/SectionTitle'
import { useCart, cartTotals } from '@/store/cart'
import { ErrorState } from '@/components/ErrorState'
import type { CreateOrderRequest } from '@/api/contracts'

export function CartPage() {
  const navigate = useNavigate()
  const { state, actions } = useCart()
  const totals = useMemo(() => cartTotals(state.items), [state.items])

  const [localError, setLocalError] = useState<string>('')

  const createM = useMutation({
    mutationFn: (req: CreateOrderRequest) => createOrder(req),
    onSuccess: (res) => {
      actions.clear()
      navigate(`/client/confirm/${encodeURIComponent(res.id)}`)
    },
    onError: (err: unknown) => {
      const msg = err instanceof Error ? err.message : 'No se pudo crear el pedido'
      setLocalError(msg)
    },
  })

  if (!state.tableId) {
    return (
      <ErrorState
        title="Primero selecciona una mesa"
        detail="No encontramos la mesa en tu sesión."
        onRetry={() => navigate('/client/table')}
      />
    )
  }

  const canSubmit = state.items.length > 0 && !createM.isPending

  function submit() {
    setLocalError('')
    if (state.items.length === 0) {
      setLocalError('Agrega al menos 1 producto.')
      return
    }
    const req: CreateOrderRequest = {
      tableId: state.tableId,
      note: state.orderNote || undefined,
      items: state.items.map((i) => ({ productId: i.productId, quantity: i.quantity, note: i.note })),
    }
    createM.mutate(req)
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Carrito"
        subtitle={`Mesa ${state.tableId}. Revisa cantidades y envía tu pedido.`}
      />

      <div className="card p-6">
        {state.items.length === 0 ? (
          <div className="text-sm text-slate-400">Tu carrito está vacío.</div>
        ) : (
          <div className="space-y-4">
            {state.items.map((i) => (
              <div key={i.productId} className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
                <div>
                  <div className="font-medium">{i.name}</div>
                  <div className="text-xs text-slate-500">{i.productId}</div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    className="btn btn-ghost cursor-pointer"
                    onClick={() => actions.setQty(i.productId, Math.max(1, i.quantity - 1))}
                    disabled={i.quantity <= 1}
                  >
                    −
                  </button>
                  <div className="min-w-10 text-center">{i.quantity}</div>
                  <button
                    className="btn btn-ghost cursor-pointer"
                    onClick={() => actions.setQty(i.productId, i.quantity + 1)}
                  >
                    +
                  </button>

                  <button
                    className="btn btn-ghost cursor-pointer"
                    onClick={() => actions.removeItem(i.productId)}
                  >
                    Quitar
                  </button>
                </div>
              </div>
            ))}

            <div className="pt-2 text-sm text-slate-300">
              Total ítems: <span className="badge">{totals.totalItems}</span>
            </div>
          </div>
        )}

        <div className="mt-6">
          <label className="text-sm text-slate-300">Nota (opcional)</label>
          <textarea
            className="input mt-2 min-h-24"
            placeholder="Ej: sin cebolla, extra salsa…"
            value={state.orderNote}
            onChange={(e) => actions.setOrderNote(e.target.value)}
          />
        </div>

        {localError ? (
          <div className="mt-4 rounded-xl bg-red-950/40 p-3 text-sm text-red-200 ring-1 ring-red-900">
            {localError}
          </div>
        ) : null}

        <div className="mt-6 flex items-center justify-between gap-3">
          <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/menu')}>
            Volver al menú
          </button>

          <button className="btn btn-primary cursor-pointer" onClick={submit} disabled={!canSubmit}>
            {createM.isPending ? 'Enviando…' : 'Enviar pedido'}
          </button>
        </div>
      </div>
    </div>
  )
}
