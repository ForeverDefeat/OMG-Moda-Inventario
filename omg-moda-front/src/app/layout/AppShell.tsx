import { Outlet, useLocation } from 'react-router-dom'
import { Footer } from './Footer'
import { HeaderBar } from './HeaderBar'

export function AppShell() {
  const location = useLocation()

  return (
    <div className="app-frame text-[var(--color-text)]">
      <div className="app-window">
        <HeaderBar />
        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <div key={location.pathname} className="page-transition">
            <Outlet />
          </div>
        </main>
        <Footer />
      </div>
    </div>
  )
}
