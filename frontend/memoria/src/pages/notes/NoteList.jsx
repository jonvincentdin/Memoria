import { useEffect } from 'react'
import { CircleAlert, FileText, Plus } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from './format'

// Lists the logged-in student's notes and provides clear loading and recovery states.
export default function NoteList() {
  const [notes, retry] = useApiData('/api/notes')

  useEffect(() => { document.title = 'Notes · Memoria' }, [])

  const newestFirst = notes.status === 'ready'
    ? [...notes.data].sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt))
    : []

  return <section>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-heading text-4xl">Notes</h1>
        <p className="mt-2 text-muted-foreground">Your notes, ready to study from.</p>
      </div>
      <Button asChild size="lg"><a href="#/notes/new" className="no-underline"><Plus aria-hidden="true" />New note</a></Button>
    </div>

    <div className="mt-8" aria-live="polite">
      {notes.status === 'loading' && <p className="text-muted-foreground">Loading your notes…</p>}

      {notes.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(notes.error)}
          <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {notes.status === 'ready' && newestFirst.length === 0 && <Card className="items-center py-12 text-center">
        <FileText className="size-8 text-muted-foreground" aria-hidden="true" />
        <CardTitle className="text-2xl font-normal">No notes yet</CardTitle>
        <CardDescription>Save what you learn by creating your first note.</CardDescription>
        <Button asChild size="lg"><a href="#/notes/new" className="no-underline"><Plus aria-hidden="true" />Create a note</a></Button>
      </Card>}

      {notes.status === 'ready' && newestFirst.length > 0 && <ul className="grid list-none gap-4 p-0 md:grid-cols-2">
        {newestFirst.map((note) => <li key={note.id}>
          <a href={`#/notes/${encodeURIComponent(note.id)}`} className="group block h-full rounded-xl text-inherit no-underline">
            <Card className="h-full transition-shadow group-hover:shadow-md">
              <CardHeader>
                <CardTitle className="text-xl font-normal [overflow-wrap:anywhere]">{note.title}</CardTitle>
                <CardDescription>Created {formatDate(note.createdAt)}</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="line-clamp-3 whitespace-pre-line text-muted-foreground [overflow-wrap:anywhere]">{note.content}</p>
              </CardContent>
            </Card>
          </a>
        </li>)}
      </ul>}
    </div>
  </section>
}
