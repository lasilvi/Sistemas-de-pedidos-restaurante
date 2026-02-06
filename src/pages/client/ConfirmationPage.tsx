import { useNavigate, useParams } from 'react-router-dom'
import { SectionTitle } from '@/components/SectionTitle'
import { Badge } from '@/components/Badge'

export function ConfirmationPage() {
  const { orderId } = useParams()
  const navigate = useNavigate()

  return (
    <div className="space-y-6">
      <SectionTitle
        title="¡Pedido enviado!"
        subtitle="La cocina recibirá el pedido. Puedes consultar el estado con tu orderId."
      />

      <div className="card p-6">
        <div className="text-sm text-slate-300">Tu identificador de pedido:</div>
        <div className="mt-2 text-lg font-semibold">
          <Badge>{orderId}</Badge>
        </div>

        <div className="mt-6 flex flex-wrap items-center gap-3">
          <button
            className="btn btn-primary cursor-pointer"
            onClick={() => navigate(`/client/status/${encodeURIComponent(orderId || '')}`)}
          >
            Ver estado
          </button>
          <button className="btn btn-ghost cursor-pointer" onClick={() => navigate('/client/table')}>
            Hacer otro pedido
          </button>
        </div>

        <div className="mt-4 text-xs text-slate-500">
          Nota: si el backend aún no implementa /orders/{'{id}'}, esta pantalla seguirá funcionando
          pero el estado no se podrá consultar.
        </div>
      </div>
    </div>
  )
}
