import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getMenu } from '@/api/menu'
import { ErrorState } from '@/components/ErrorState'
import { Loading } from '@/components/Loading'
import { SectionTitle } from '@/components/SectionTitle'
import { useCart, cartTotals } from '@/store/cart'
import { Badge } from '@/components/Badge'

export function MenuPage() {
  const navigate = useNavigate()
  const { state, actions } = useCart()

  const menuQ = useQuery({
    queryKey: ['menu'],
    queryFn: getMenu,
  })

  const totals = useMemo(() => cartTotals(state.items), [state.items])

  if (!state.tableId) {
    return (
      <ErrorState
        title="Primero selecciona una mesa"
        detail="No encontramos la mesa en tu sesión."
        onRetry={() => navigate('/client/table')}
      />
    )
  }

  if (menuQ.isLoading) return <Loading label="Cargando menú…" />
  if (menuQ.isError) {
    const err = menuQ.error as Error
    return (
      <ErrorState
        title="No pudimos cargar el menú"
        detail={err.message}
        onRetry={() => menuQ.refetch()}
      />
    )
  }

  const products = menuQ.data ?? []

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Menú"
        subtitle={`Mesa ${state.tableId}. Elige productos y agrégalos al carrito.`}
        right={
          <button className="btn btn-primary cursor-pointer" onClick={() => navigate('/client/cart')}>
            Ver carrito <span className="ml-2 badge">{totals.totalItems}</span>
          </button>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {products.map((p) => (
          <div key={p.id} className="card p-5">
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="text-base font-semibold">{p.name}</div>
                {p.description ? <div className="mt-1 text-sm text-slate-400">{p.description}</div> : null}
              </div>
              <Badge>{p.id}</Badge>
            </div>

            <div className="mt-4 flex items-center justify-between">
              <button className="btn btn-ghost cursor-pointer" onClick={() => actions.addItem(p)}>
                + Agregar
              </button>
              <div className="text-xs text-slate-500">
                En carrito:{' '}
                <span className="text-slate-200">
                  {state.items.find((i) => i.productId === p.id)?.quantity ?? 0}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="flex items-center justify-between">
        <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/table')}>
          Cambiar mesa
        </button>

        <button className="btn btn-primary cursor-pointer" onClick={() => navigate('/client/cart')}>
          Continuar ({totals.totalItems} ítems)
        </button>
      </div>
    </div>
  )
}
