import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert, FileText, Pencil, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import FlashcardSection from './FlashcardSection'
import { formatDate } from './format'

// Reading view for one reviewer: content, linked notes, edit and delete.
export default function ReviewerDetail({ id }) {
  const [reviewer, retry] = useApiData(`/api/reviewers/${id}`)
  const [confirming, setConfirming] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const title = reviewer.status === 'ready' ? reviewer.data.title : 'Reviewer'
  useEffect(() => { document.title = `${title} · Memoria` }, [title])

  // Deletes after confirmation. A blocked delete (linked to a quiz) shows the backend's message.
  async function deleteReviewer() {
    setDeleting(true)
    setDeleteError('')
    try {
      await apiRequest(`/api/reviewers/${id}`, { method: 'DELETE' })
      window.location.hash = '#/reviewers'
    } catch (error) {
      setDeleteError(errorMessage(error))
      setConfirming(false)
      setDeleting(false)
    }
  }

  const backLink = <a href="#/reviewers" className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />All reviewers</a>

  if (reviewer.status === 'loading') return <section className="mx-auto max-w-3xl">{backLink}<p className="mt-6 text-muted-foreground" aria-live="polite">Loading reviewer…</p></section>

  if (reviewer.status === 'error') {
    const notFound = reviewer.error?.status === 404
    return <section className="mx-auto max-w-3xl">
      {backLink}
      <h1 className="mt-4 font-heading text-4xl">{notFound ? 'Reviewer not found' : 'Something went wrong'}</h1>
      <Alert variant="destructive" className="mt-6">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {notFound ? 'This reviewer doesn’t exist or isn’t yours.' : errorMessage(reviewer.error)}
          {!notFound && <Button variant="outline" size="sm" onClick={retry}>Try again</Button>}
        </AlertDescription>
      </Alert>
    </section>
  }

  const { style, content, createdAt, notes } = reviewer.data
  return <article className="mx-auto max-w-3xl">
    {backLink}
    <header className="mt-4 flex flex-wrap items-start justify-between gap-4">
      <div className="min-w-0">
        <h1 className="font-heading text-4xl [overflow-wrap:anywhere]">{title}</h1>
        <p className="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          <Badge variant="secondary">{style}</Badge>Created {formatDate(createdAt)}
        </p>
      </div>
      <div className="flex gap-2">
        <Button asChild variant="outline" size="lg"><a href={`#/reviewers/${id}/edit`} className="no-underline"><Pencil aria-hidden="true" />Edit</a></Button>
        <Button variant="destructive" size="lg" onClick={() => { setConfirming(true); setDeleteError('') }} disabled={confirming}><Trash2 aria-hidden="true" />Delete</Button>
      </div>
    </header>

    {confirming && <Alert className="mt-6" role="alertdialog" aria-labelledby="confirm-delete">
      <Trash2 aria-hidden="true" />
      <AlertDescription className="grid gap-3">
        <p id="confirm-delete" className="text-foreground">Delete “{title}”? This can’t be undone. Your notes are not deleted.</p>
        <div className="flex gap-2">
          <Button variant="destructive" onClick={deleteReviewer} disabled={deleting} autoFocus>{deleting ? 'Deleting…' : 'Yes, delete'}</Button>
          <Button variant="outline" onClick={() => setConfirming(false)} disabled={deleting}>Cancel</Button>
        </div>
      </AlertDescription>
    </Alert>}

    {deleteError && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{deleteError}</AlertDescription>
    </Alert>}

    <Card className="mt-8">
      <CardContent>
        <div className="text-base leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]">{content}</div>
      </CardContent>
    </Card>

    <Card className="mt-6" aria-labelledby="linked-notes-heading">
      <CardHeader>
        <CardTitle id="linked-notes-heading" className="text-xl font-normal">Linked notes ({notes.length})</CardTitle>
      </CardHeader>
      <CardContent>
        <ul className="grid list-none gap-2 p-0">
          {notes.map((note) => <li key={note.id}>
            <a href={`#/notes/${note.id}`} className="flex items-center gap-2 rounded-lg border px-4 py-3 no-underline hover:bg-muted">
              <FileText className="size-4 shrink-0" aria-hidden="true" /><span className="[overflow-wrap:anywhere]">{note.title}</span>
            </a>
          </li>)}
        </ul>
      </CardContent>
    </Card>

    <FlashcardSection reviewerId={id} />
  </article>
}
