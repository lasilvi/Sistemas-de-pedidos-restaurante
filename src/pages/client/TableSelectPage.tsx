import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { SectionTitle } from '@/components/SectionTitle'
import { useCart } from '@/store/cart'
import { Badge } from '@/components/Badge'

export function TableSelectPage() {
  const navigate = useNavigate()
  const { state, actions } = useCart()

  const tables = useMemo(() => Array.from({ length: 9 }, (_, i) => i + 1), [])

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Selecciona tu mesa"
        subtitle="El pedido quedará asociado a la mesa (1 a 9)."
      />

      <div className="card p-6">
        <div className="grid grid-cols-3 gap-3 sm:grid-cols-5">
          {tables.map((n) => (
            <button
              key={n}
              className={`btn cursor-pointer py-6 text-base ${
                state.tableId === n ? 'btn-primary' : 'btn-ghost'
              }`}
              onClick={() => actions.setTable(n)}
            >
              Mesa {n}
            </button>
          ))}
        </div>

        <div className="mt-6 flex items-center justify-between gap-3">
          <div className="text-sm text-slate-400">
            Mesa seleccionada:{' '}
            {state.tableId ? (
              <Badge>Mesa {state.tableId}</Badge>
            ) : (
              <span className="text-slate-500">—</span>
            )}
          </div>

          <button
            className="btn btn-primary cursor-pointer"
            disabled={!state.tableId}
            onClick={() => navigate('/client/menu')}
          >
            Ir al menú
          </button>
        </div>
      </div>

      <div className="text-xs text-slate-500">
        Tip: si ya tienes un <strong>orderId</strong>, puedes ir a{' '}
        <a className="underline" href="/client/status">
          consultar estado
        </a>
        .
      </div>
    </div>
  )
}
