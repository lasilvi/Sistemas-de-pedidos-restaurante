import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { TopNav } from '@/components/TopNav'

export function AppLayout() {
  const location = useLocation()
  const navigate = useNavigate()

  const isKitchen = location.pathname.startsWith('/kitchen')

  return (
    <div className="min-h-screen">
      <TopNav
        mode={isKitchen ? 'kitchen' : 'client'}
        onSwitch={(m) => navigate(m === 'kitchen' ? '/kitchen' : '/client/table')}
      />

      <main className="mx-auto w-full max-w-5xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
