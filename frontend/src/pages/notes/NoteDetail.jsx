import { useEffect, useState } from 'react'
import { Archive, ArchiveRestore, ArrowLeft, CircleAlert, FileText, Pencil, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from './format'

// Shows one owned note as plain text, with edit, archive/restore, and delete actions.
export default function NoteDetail({ id }) {
  const [note, retry] = useApiData(`/api/notes/${encodeURIComponent(id)}`)
  const [confirming, setConfirming] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [updatingArchive, setUpdatingArchive] = useState(false)
  const [actionError, setActionError] = useState('')

  const title = note.status === 'ready' ? note.data.title : 'Note'
  useEffect(() => { document.title = `${title} · Memoria` }, [title])

  // Deletes only after confirmation and preserves the backend's blocked-delete explanation.
  async function deleteNote() {
    setDeleting(true)
    setActionError('')
    try {
      await apiRequest(`/api/notes/${encodeURIComponent(id)}`, { method: 'DELETE' })
      window.location.hash = '#/notes'
    } catch (error) {
      setActionError(errorMessage(error))
      setConfirming(false)
      setDeleting(false)
    }
  }

  async function toggleArchive(archived) {
    setUpdatingArchive(true)
    setActionError('')
    try {
      await apiRequest(`/api/notes/${encodeURIComponent(id)}/${archived ? 'restore' : 'archive'}`, { method: 'POST' })
      retry()
    } catch (error) {
      setActionError(errorMessage(error))
    } finally {
      setUpdatingArchive(false)
    }
  }

  const backLink = <a href="#/notes" className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />All notes</a>

  if (note.status === 'loading') return <section className="mx-auto max-w-3xl">{backLink}<p className="mt-6 text-muted-foreground" aria-live="polite">Loading note…</p></section>

  if (note.status === 'error') {
    const notFound = note.error?.status === 404
    return <section className="mx-auto max-w-3xl">
      {backLink}
      <h1 className="mt-4 font-heading text-4xl">{notFound ? 'Note not found' : 'Something went wrong'}</h1>
      <Alert variant="destructive" className="mt-6">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {notFound ? 'This note doesn’t exist or isn’t yours.' : errorMessage(note.error)}
          {!notFound && <Button variant="outline" size="sm" onClick={retry}>Try again</Button>}
        </AlertDescription>
      </Alert>
    </section>
  }

  const { content, createdAt, archived, archivedAt, sourceFilename } = note.data
  return <article className="mx-auto max-w-3xl">
    {backLink}
    <header className="mt-4 flex flex-wrap items-start justify-between gap-4">
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <h1 className="font-heading text-4xl [overflow-wrap:anywhere]">{title}</h1>
          {archived && <Badge variant="secondary">Archived</Badge>}
        </div>
        <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-muted-foreground">
          <span>Created {formatDate(createdAt)}</span>
          {archived && archivedAt && <span>Archived {formatDate(archivedAt)}</span>}
          {sourceFilename && <span className="inline-flex items-center gap-1 [overflow-wrap:anywhere]"><FileText className="size-3.5 shrink-0" aria-hidden="true" />Imported from {sourceFilename}</span>}
        </div>
      </div>
      <div className="flex flex-wrap gap-2">
        <Button asChild variant="outline" size="lg"><a href={`#/notes/${encodeURIComponent(id)}/edit`} className="no-underline"><Pencil aria-hidden="true" />Edit</a></Button>
        <Button
          variant="outline"
          size="lg"
          onClick={() => toggleArchive(archived)}
          disabled={updatingArchive || deleting}
        >
          {archived ? <ArchiveRestore aria-hidden="true" /> : <Archive aria-hidden="true" />}
          {updatingArchive ? (archived ? 'Restoring…' : 'Archiving…') : (archived ? 'Restore' : 'Archive')}
        </Button>
        <Button variant="destructive" size="lg" onClick={() => { setConfirming(true); setActionError('') }} disabled={confirming || updatingArchive}><Trash2 aria-hidden="true" />Delete</Button>
      </div>
    </header>

    {confirming && <Alert className="mt-6" role="alertdialog" aria-labelledby="confirm-delete">
      <Trash2 aria-hidden="true" />
      <AlertDescription className="grid gap-3">
        <p id="confirm-delete" className="text-foreground">Delete “{title}”? This can’t be undone.</p>
        <div className="flex gap-2">
          <Button variant="destructive" onClick={deleteNote} disabled={deleting} autoFocus>{deleting ? 'Deleting…' : 'Yes, delete'}</Button>
          <Button variant="outline" onClick={() => setConfirming(false)} disabled={deleting}>Cancel</Button>
        </div>
      </AlertDescription>
    </Alert>}

    {actionError && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{actionError}</AlertDescription>
    </Alert>}

    <Card className="mt-8">
      <CardContent>
        <div className="text-base leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]">{content}</div>
      </CardContent>
    </Card>
  </article>
}
