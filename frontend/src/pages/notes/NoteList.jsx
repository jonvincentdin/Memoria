import { useEffect, useState } from 'react'
import { Archive, CircleAlert, FileText, Plus, Upload } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from './format'

// Lists the logged-in student's active or archived notes with clear loading and recovery states.
export default function NoteList() {
  const [view, setView] = useState('active')
  const showingArchived = view === 'archived'
  const [notes, retry] = useApiData(showingArchived ? '/api/notes?archived=true' : '/api/notes?archived=false')

  useEffect(() => { document.title = 'Notes · Memoria' }, [])

  const items = notes.status === 'ready' ? notes.data : []

  return <section>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-heading text-4xl">Notes</h1>
        <p className="mt-2 text-muted-foreground">Your notes, ready to study from.</p>
      </div>
      <div className="flex flex-wrap gap-2">
        <Button asChild variant="outline" size="lg"><a href="#/notes/import" className="no-underline"><Upload aria-hidden="true" />Import file</a></Button>
        <Button asChild size="lg"><a href="#/notes/new" className="no-underline"><Plus aria-hidden="true" />New note</a></Button>
      </div>
    </div>

    <div role="tablist" aria-label="Note status" className="mt-6 flex gap-2 border-b pb-3">
      <Button
        type="button"
        role="tab"
        aria-selected={!showingArchived}
        variant={!showingArchived ? 'default' : 'outline'}
        size="sm"
        onClick={() => setView('active')}
      >
        Active
      </Button>
      <Button
        type="button"
        role="tab"
        aria-selected={showingArchived}
        variant={showingArchived ? 'default' : 'outline'}
        size="sm"
        onClick={() => setView('archived')}
      >
        <Archive className="size-4" aria-hidden="true" />
        Archived
      </Button>
    </div>

    <div className="mt-6" aria-live="polite">
      {notes.status === 'loading' && <p className="text-muted-foreground">Loading your notes…</p>}

      {notes.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(notes.error)}
          <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {notes.status === 'ready' && items.length === 0 && <Card className="items-center py-12 text-center">
        {showingArchived
          ? <>
            <Archive className="size-8 text-muted-foreground" aria-hidden="true" />
            <CardTitle className="text-2xl font-normal">No archived notes</CardTitle>
            <CardDescription>Notes you archive will appear here until you restore or delete them.</CardDescription>
          </>
          : <>
            <FileText className="size-8 text-muted-foreground" aria-hidden="true" />
            <CardTitle className="text-2xl font-normal">No notes yet</CardTitle>
            <CardDescription>Save what you learn by creating a note or importing a file.</CardDescription>
            <div className="flex flex-wrap justify-center gap-2">
              <Button asChild size="lg"><a href="#/notes/new" className="no-underline"><Plus aria-hidden="true" />Create a note</a></Button>
              <Button asChild variant="outline" size="lg"><a href="#/notes/import" className="no-underline"><Upload aria-hidden="true" />Import file</a></Button>
            </div>
          </>}
      </Card>}

      {notes.status === 'ready' && items.length > 0 && <ul className="grid list-none gap-4 p-0 md:grid-cols-2">
        {items.map((note) => <li key={note.id}>
          <a href={`#/notes/${encodeURIComponent(note.id)}`} className="group block h-full rounded-xl text-inherit no-underline">
            <Card className="h-full transition-shadow group-hover:shadow-md">
              <CardHeader>
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <CardTitle className="text-xl font-normal [overflow-wrap:anywhere]">{note.title}</CardTitle>
                  {note.archived && <Badge variant="secondary">Archived</Badge>}
                </div>
                <CardDescription className="flex flex-wrap items-center gap-x-3 gap-y-1">
                  <span>{note.archived && note.archivedAt ? `Archived ${formatDate(note.archivedAt)}` : `Created ${formatDate(note.createdAt)}`}</span>
                  {note.sourceFilename && <span className="truncate">From {note.sourceFilename}</span>}
                </CardDescription>
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
