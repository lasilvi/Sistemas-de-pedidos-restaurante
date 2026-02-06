export function ErrorState({
  title = 'Ocurrió un error',
  detail,
  onRetry,
}: {
  title?: string
  detail?: string
  onRetry?: () => void
}) {
  return (
    <div className="card p-6">
      <div className="text-base font-semibold">{title}</div>
      {detail ? <div className="mt-2 text-sm text-slate-300">{detail}</div> : null}
      {onRetry ? (
        <button className="btn btn-primary mt-4 cursor-pointer" onClick={onRetry}>
          Reintentar
        </button>
      ) : null}
    </div>
  )
}
