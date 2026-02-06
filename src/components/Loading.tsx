export function Loading({ label = 'Cargando…' }: { label?: string }) {
  return (
    <div className="card p-6">
      <div className="flex items-center gap-3">
        <div className="h-4 w-4 animate-spin rounded-full border-2 border-slate-400 border-t-transparent" />
        <div className="text-sm text-slate-300">{label}</div>
      </div>
    </div>
  )
}
