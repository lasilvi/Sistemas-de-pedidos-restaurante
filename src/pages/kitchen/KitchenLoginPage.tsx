import { useNavigate } from 'react-router-dom'
import { SectionTitle } from '@/components/SectionTitle'

export function KitchenLoginPage() {
  const navigate = useNavigate()

  function submit() {
    navigate('/kitchen/board', { replace: true })
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Cocina"
        subtitle="Acceso directo en modo mock."
      />

      <div className="card p-6">
        <div className="flex items-center justify-end">
          <button className="btn btn-primary cursor-pointer" onClick={submit}>
            Entrar a cocina
          </button>
        </div>
      </div>
    </div>
  )
}
