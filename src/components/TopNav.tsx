type Mode = 'client' | 'kitchen'

export function TopNav({
  mode,
  onSwitch,
}: {
  mode: Mode
  onSwitch: (m: Mode) => void
}) {
  return (
    <header className="sticky top-0 z-10 border-b border-slate-800 bg-slate-950/80 backdrop-blur">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="grid h-9 w-9 place-items-center rounded-xl bg-slate-900 ring-1 ring-slate-800">
            🍽️
          </div>
          <div>
            <div className="text-sm font-semibold">Pedidos — Restaurante</div>
            <div className="text-xs text-slate-400">
              MVP: Mesa → Menú → Carrito → Confirmación
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            className={`btn cursor-pointer ${mode === 'client' ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => onSwitch('client')}
          >
            Cliente
          </button>
          <button
            className={`btn cursor-pointer ${mode === 'kitchen' ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => onSwitch('kitchen')}
          >
            Cocina
          </button>
        </div>
      </div>
    </header>
  )
}
