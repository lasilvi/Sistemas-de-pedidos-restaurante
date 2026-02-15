import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ENV } from '@/api/env'
import { setKitchenToken } from '@/store/kitchenAuth'
import { SectionTitle } from '@/components/SectionTitle'

export function KitchenLoginPage() {
  const navigate = useNavigate()
  const [pin, setPin] = useState('')
  const [error, setError] = useState('')

  function submit() {
    if (pin !== ENV.KITCHEN_PIN) {
      setError('PIN de cocina invalido')
      return
    }
    const token = ENV.KITCHEN_FIXED_TOKEN || pin
    setKitchenToken(token)
    setError('')
    navigate('/kitchen/board', { replace: true })
  }

  return (
    <div className="space-y-6">
      <SectionTitle
        title="Cocina"
        subtitle="Ingresa el PIN para habilitar acciones de cocina."
      />

      <div className="card space-y-4 p-6">
        <label className="block text-sm text-slate-300" htmlFor="kitchen-pin">
          PIN de cocina
        </label>
        <input
          id="kitchen-pin"
          type="password"
          value={pin}
          onChange={(e) => setPin(e.target.value)}
          className="w-full rounded-xl border border-slate-700 bg-slate-900 px-3 py-2 text-slate-100"
          placeholder="Ingresa PIN"
        />
        {error ? <div className="text-sm text-rose-400">{error}</div> : null}
        <div className="flex items-center justify-end">
          <button className="btn btn-primary cursor-pointer" onClick={submit}>
            Entrar a cocina
          </button>
        </div>
      </header>

      <main className="page-wrap flex items-center justify-center py-10">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.28 }}
          className="w-full max-w-md"
        >
          <Card className="p-8">
            <div className="mb-8 flex flex-col items-center text-center">
              <div className="mb-4 rounded-full bg-accent p-4 text-accent-foreground">
                <ChefHat className="h-12 w-12" />
              </div>
              <h1 className="text-3xl font-semibold">Panel de cocina</h1>
              <p className="mt-2 text-sm text-muted-foreground">Ingresa para gestionar pedidos.</p>
            </div>

            <form onSubmit={handleLogin} className="space-y-4">
              <label className="block text-sm font-medium" htmlFor="kitchen-pass">
                PIN
              </label>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="kitchen-pass"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Ingresa tu PIN"
                  className="pl-9"
                  required
                />
              </div>

              <Button className="h-11 w-full" disabled={loading} type="submit">
                {loading ? 'Verificando...' : 'Ingresar'}
              </Button>
            </form>

            <Card className="mt-5 border-none bg-muted/60 p-3 text-center text-xs text-muted-foreground">
              Demo: usa el PIN "{ENV.KITCHEN_PIN}"
            </Card>
          </Card>
        </motion.div>
      </main>
    </div>
  )
}
