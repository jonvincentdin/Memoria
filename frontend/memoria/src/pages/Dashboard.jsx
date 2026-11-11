import { useEffect } from 'react'
import { ArrowRight, Mail, CalendarDays } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { SECTIONS } from '@/sections'

function memberSince(createdAt) {
  const date = createdAt ? new Date(createdAt) : null
  return date && !Number.isNaN(date.getTime())
    ? date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })
    : null
}

export default function Dashboard({ user }) {
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'
  const joined = memberSince(user.createdAt)

  useEffect(() => { document.title = 'Dashboard · Memoria' }, [])

  return <>
    <section className="grid gap-6 lg:grid-cols-[1fr_360px] lg:items-end">
      <div>
        <p className="mb-2 text-sm text-muted-foreground">{new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' })}</p>
        <h1 className="font-heading text-4xl leading-tight tracking-tight [overflow-wrap:anywhere] sm:text-5xl">{greeting}, {user.name}.</h1>
        <p className="mt-3 text-muted-foreground">Choose where to start today.</p>
      </div>
      <Card aria-labelledby="account-heading">
        <CardHeader>
          <CardDescription id="account-heading">Your account</CardDescription>
          <CardTitle className="text-xl font-normal [overflow-wrap:anywhere]">{user.name}</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-2 text-muted-foreground">
          <p className="flex min-w-0 items-center gap-2"><Mail className="size-4 shrink-0" aria-hidden="true" /><span className="truncate">{user.email}</span></p>
          {joined && <p className="flex items-center gap-2"><CalendarDays className="size-4 shrink-0" aria-hidden="true" />Member since {joined}</p>}
        </CardContent>
      </Card>
    </section>

    <section aria-labelledby="sections-heading" className="mt-12">
      <h2 id="sections-heading" className="mb-5 font-heading text-3xl">Study tools</h2>
      <ul className="grid list-none gap-4 p-0 sm:grid-cols-2 lg:grid-cols-4">
        {SECTIONS.map(({ route, label, icon: Icon, description, ready }) => <li key={route}>
          <a href={`#/${route}`} className="group block h-full rounded-xl text-inherit no-underline">
            <Card className="h-full transition-shadow group-hover:shadow-md">
              <CardHeader>
                <span className="mb-2 grid size-10 place-items-center rounded-lg bg-secondary text-secondary-foreground"><Icon className="size-5" aria-hidden="true" /></span>
                <CardTitle className="text-xl font-normal">{label}</CardTitle>
                <CardDescription>{description}</CardDescription>
              </CardHeader>
              <CardFooter className="mt-auto justify-between text-xs text-muted-foreground">
                {ready ? <span>Open</span> : <Badge variant="secondary">Coming soon</Badge>}
                <ArrowRight className="size-4 text-primary transition-transform group-hover:translate-x-0.5" aria-hidden="true" />
              </CardFooter>
            </Card>
          </a>
        </li>)}
      </ul>
    </section>
  </>
}
