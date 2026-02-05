import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ENV } from '@/api/env'
import { SectionTitle } from '@/components/SectionTitle'
import { getKitchenToken, setKitchenToken } from '@/store/kitchenAuth'

export function KitchenLoginPage() {
  const navigate = useNavigate()
  const [pin, setPin] = useState('')

  useEffect(() => {
    const token = getKitchenToken()
    if (token) navigate('/kitchen/board', { replace: true })
  }, [navigate])

  function submit() {
    const p = pin.trim()
    if (!p) return
    // En MVP, el PIN es el token
    if (p !== ENV.KITCHEN_PIN) {
      alert('PIN incorrecto')
      return
    }
    setKitchenToken(p)
    navigate('/kitchen/board', { replace: true })
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Cocina"
        subtitle="Ingresa el PIN para ver y gestionar pedidos."
      />

      <div className="card p-6">
        <label className="text-sm text-slate-300">PIN</label>
        <input
          className="input mt-2"
          placeholder="Ej: 1234"
          value={pin}
          onChange={(e) => setPin(e.target.value)}
          onKeyDown={(e) => (e.key === 'Enter' ? submit() : null)}
        />

        <div className="mt-4 flex items-center justify-end">
          <button className="btn btn-primary cursor-pointer" onClick={submit}>
            Entrar
          </button>
        </div>

        <div className="mt-4 text-xs text-slate-500">
          Seguridad mínima (MVP): PIN compartido por el restaurante. Se configura en <code>.env</code>.
        </div>
      </div>
    </div>
  )
}
