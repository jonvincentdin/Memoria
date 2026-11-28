import { useEffect } from 'react'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'

// Placeholder for a study section whose screen is not built yet.
export default function ComingSoon({ section }) {
  const { label, icon: Icon, description } = section
  useEffect(() => { document.title = `${label} · Memoria` }, [label])

  return <section className="mx-auto flex max-w-md flex-col items-center py-12 text-center">
    <span className="mb-6 grid size-16 place-items-center rounded-2xl bg-secondary text-secondary-foreground"><Icon className="size-8" aria-hidden="true" /></span>
    <h1 className="font-heading text-4xl">{label}</h1>
    <p className="mt-3 text-muted-foreground">{description}</p>
    <p className="mt-6 rounded-lg border bg-card px-4 py-3 text-sm">This section isn’t available yet. It’s still being built.</p>
    <Button asChild variant="outline" size="lg" className="mt-8">
      <a href="#/dashboard" className="no-underline"><ArrowLeft aria-hidden="true" />Back to dashboard</a>
    </Button>
  </section>
}
