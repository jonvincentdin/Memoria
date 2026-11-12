import { useEffect } from 'react'
import { CircleAlert, ClipboardList, FileText, Plus } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from './format'

// Lists the logged-in student's reviewers with loading, empty and error states.
export default function ReviewerList() {
  const [reviewers, retry] = useApiData('/api/reviewers')

  useEffect(() => { document.title = 'Reviewers · Memoria' }, [])

  return <section>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-heading text-4xl">Reviewers</h1>
        <p className="mt-2 text-muted-foreground">Quick recaps made from your notes.</p>
      </div>
      <Button asChild size="lg"><a href="#/reviewers/new" className="no-underline"><Plus aria-hidden="true" />New reviewer</a></Button>
    </div>

    <div className="mt-8" aria-live="polite">
      {reviewers.status === 'loading' && <p className="text-muted-foreground">Loading your reviewers…</p>}

      {reviewers.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(reviewers.error)}
          <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {reviewers.status === 'ready' && reviewers.data.length === 0 && <Card className="items-center py-12 text-center">
        <span className="grid size-14 place-items-center rounded-2xl bg-secondary text-secondary-foreground"><ClipboardList className="size-7" aria-hidden="true" /></span>
        <CardTitle className="text-2xl font-normal">No reviewers yet</CardTitle>
        <CardDescription>Create your first reviewer from one or more of your notes.</CardDescription>
        <Button asChild size="lg"><a href="#/reviewers/new" className="no-underline"><Plus aria-hidden="true" />New reviewer</a></Button>
      </Card>}

      {reviewers.status === 'ready' && reviewers.data.length > 0 && <ul className="grid list-none gap-4 p-0 md:grid-cols-2">
        {reviewers.data.map((reviewer) => <li key={reviewer.id}>
          <a href={`#/reviewers/${reviewer.id}`} className="group block h-full rounded-xl text-inherit no-underline">
            <Card className="h-full transition-shadow group-hover:shadow-md">
              <CardHeader>
                <div className="flex items-start justify-between gap-3">
                  <CardTitle className="text-xl font-normal [overflow-wrap:anywhere]">{reviewer.title}</CardTitle>
                  <Badge variant="secondary">{reviewer.style}</Badge>
                </div>
                <CardDescription>Created {formatDate(reviewer.createdAt)}</CardDescription>
              </CardHeader>
              <CardContent className="grid gap-3">
                <p className="line-clamp-3 whitespace-pre-line text-muted-foreground">{reviewer.content}</p>
                <p className="flex flex-wrap items-center gap-1.5 text-xs text-muted-foreground">
                  <FileText className="size-3.5" aria-hidden="true" />
                  {reviewer.notes.length === 1 ? 'From 1 note:' : `From ${reviewer.notes.length} notes:`}
                  {reviewer.notes.map((note) => <Badge key={note.id} variant="outline">{note.title}</Badge>)}
                </p>
              </CardContent>
            </Card>
          </a>
        </li>)}
      </ul>}
    </div>
  </section>
}
