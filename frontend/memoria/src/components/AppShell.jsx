import { useState } from 'react'
import { CircleAlert, LayoutDashboard, LogOut } from 'lucide-react'
import { ApiError } from '@/api'
import { BookMark } from '@/components/Icons'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { SECTIONS } from '@/sections'

const NAV = [{ route: 'dashboard', label: 'Dashboard', icon: LayoutDashboard }, ...SECTIONS]

// Layout for every signed-in screen: brand, section navigation, account and logout.
export default function AppShell({ user, current, onLogout, children }) {
  const [logoutError, setLogoutError] = useState('')
  const [loggingOut, setLoggingOut] = useState(false)

  async function handleLogout() {
    setLogoutError('')
    setLoggingOut(true)
    try {
      await onLogout()
    } catch (error) {
      setLogoutError(error instanceof ApiError
        ? error.message
        : 'Memoria could not be reached. Check your connection and try again.')
    } finally {
      setLoggingOut(false)
    }
  }

  return <div className="flex min-h-svh flex-col">
    <header className="sticky top-0 z-10 border-b bg-background/85 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4 sm:px-8">
        <a className="flex items-center gap-2.5 text-primary no-underline" href="#/dashboard" aria-label="Memoria dashboard">
          <BookMark />
          <span className="font-heading text-2xl">memoria<span className="text-ring">.</span></span>
        </a>
        <div className="flex min-w-0 items-center gap-3">
          <Avatar>
            <AvatarFallback className="bg-primary font-semibold text-primary-foreground">{user.name[0].toUpperCase()}</AvatarFallback>
          </Avatar>
          <span className="hidden truncate text-sm text-muted-foreground sm:block">{user.name}</span>
          <Button variant="outline" size="lg" onClick={handleLogout} disabled={loggingOut}>
            <LogOut aria-hidden="true" />
            {loggingOut ? 'Logging out…' : 'Log out'}
          </Button>
        </div>
      </div>
      <nav aria-label="Main" className="mx-auto max-w-6xl overflow-x-auto px-4 sm:px-8">
        <ul className="flex list-none gap-1 p-0">
          {NAV.map(({ route, label, icon: Icon }) => <li key={route}>
            <a
              href={`#/${route}`}
              aria-current={current === route ? 'page' : undefined}
              className={cn(
                'flex items-center gap-2 border-b-2 border-transparent px-3 py-3 text-sm whitespace-nowrap text-muted-foreground no-underline hover:text-primary',
                current === route && 'border-primary font-semibold text-primary',
              )}
            >
              <Icon className="size-4" aria-hidden="true" />{label}
            </a>
          </li>)}
        </ul>
      </nav>
    </header>

    <main id="auth-main" tabIndex="-1" className="mx-auto w-full max-w-6xl flex-1 px-4 py-10 outline-none sm:px-8 sm:py-14">
      {logoutError && <Alert variant="destructive" className="mb-8" role="alert">
        <CircleAlert aria-hidden="true" />
        <AlertDescription>{logoutError}</AlertDescription>
      </Alert>}
      {children}
    </main>
  </div>
}
