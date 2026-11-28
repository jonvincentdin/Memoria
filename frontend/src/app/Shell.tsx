import { useEffect, useRef, useState, type ReactNode } from 'react'
import { LogOut } from 'lucide-react'
import type { User } from '../features/auth/types'
import { Brand, Notice } from '../shared/ui/common'
import { Button } from '../shared/ui/button'
import { navigation } from './navigation'

export function Shell({ user, current, logout, children }: { user: User; current: string; logout: () => Promise<void>; children: ReactNode }) {
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const nav = useRef<HTMLElement>(null)
  useEffect(() => {
    const node = nav.current
    if (!node) return
    const keepVisible = () => node.querySelector<HTMLElement>('[aria-current="page"]')?.scrollIntoView({ block: 'nearest', inline: 'nearest' })
    const observer = new ResizeObserver(keepVisible)
    observer.observe(node)
    keepVisible()
    return () => observer.disconnect()
  }, [current])
  async function signOut() { if (pending) return; setPending(true); setError(null); try { await logout() } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <div className="app-layout"><a className="skip-link" href="#main-content" onClick={event => { event.preventDefault(); document.getElementById('main-content')?.focus() }}>Skip to content</a><header className="app-header"><div className="header-top"><Brand /><div className="account-menu"><span className="account-name">{user.name}</span><span className="avatar" aria-hidden="true">{(user.name || user.email).slice(0, 1).toUpperCase()}</span><Button variant="ghost" onClick={signOut} disabled={pending}><LogOut aria-hidden="true" /><span>{pending ? 'Logging out…' : 'Log out'}</span></Button></div></div><nav ref={nav} aria-label="Main navigation">{navigation.map(({ path, label, icon: Icon }) => <a key={path} href={`#/${path}`} aria-current={current === path ? 'page' : undefined}><Icon size={17} aria-hidden="true" />{label}</a>)}</nav></header><main id="main-content" tabIndex={-1} className="app-main">{!!error && <Notice error={error} />}{children}</main></div>
}
