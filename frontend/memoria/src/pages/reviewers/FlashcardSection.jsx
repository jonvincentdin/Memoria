import { useState } from 'react'
import { CircleAlert, Pencil, Plus, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'

export default function FlashcardSection({ reviewerId }) {
  const [flashcards, retry] = useApiData(`/api/flashcards?reviewerId=${encodeURIComponent(reviewerId)}`)
  const [confirmingId, setConfirmingId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [deleteError, setDeleteError] = useState('')

  async function deleteFlashcard(flashcardId) {
    setDeletingId(flashcardId)
    setDeleteError('')
    try {
      await apiRequest(`/api/flashcards/${encodeURIComponent(flashcardId)}`, { method: 'DELETE' })
      setConfirmingId(null)
      setDeletingId(null)
      retry()
    } catch (error) {
      setDeleteError(errorMessage(error))
      setConfirmingId(null)
      setDeletingId(null)
    }
  }

  return <section className="mt-8" aria-labelledby="flashcards-heading">
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h2 id="flashcards-heading" className="font-heading text-2xl">Flashcards</h2>
        <p className="mt-1 text-sm text-muted-foreground">Cards for studying this reviewer.</p>
      </div>
      <div className="flex flex-wrap gap-2">
        {flashcards.status === 'ready' && flashcards.data.length > 0 && <Button asChild variant="outline">
          <a href={`#/reviewers/${encodeURIComponent(reviewerId)}/study`} className="no-underline">Study cards</a>
        </Button>}
        <Button asChild>
          <a href={`#/reviewers/${encodeURIComponent(reviewerId)}/flashcards/new`} className="no-underline"><Plus aria-hidden="true" />New flashcard</a>
        </Button>
      </div>
    </div>

    {deleteError && <Alert variant="destructive" className="mt-4" role="alert">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{deleteError}</AlertDescription>
    </Alert>}

    <div className="mt-4" aria-live="polite">
      {flashcards.status === 'loading' && <p className="text-sm text-muted-foreground">Loading flashcards…</p>}

      {flashcards.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(flashcards.error)}
          <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {flashcards.status === 'ready' && flashcards.data.length === 0 && <Card>
        <CardContent className="grid gap-3 py-6">
          <p className="font-medium">No flashcards yet</p>
          <p className="text-sm text-muted-foreground">Add a front and back to make a card for this reviewer.</p>
          <div><Button asChild variant="outline"><a href={`#/reviewers/${encodeURIComponent(reviewerId)}/flashcards/new`} className="no-underline"><Plus aria-hidden="true" />Create flashcard</a></Button></div>
        </CardContent>
      </Card>}

      {flashcards.status === 'ready' && flashcards.data.length > 0 && <ul className="grid list-none gap-3 p-0">
        {flashcards.data.map((flashcard) => <li key={flashcard.id}>
          <Card>
            <CardHeader className="flex flex-row items-start justify-between gap-4">
              <CardTitle className="min-w-0 text-base font-medium [overflow-wrap:anywhere]">{flashcard.front}</CardTitle>
              <div className="flex shrink-0 gap-2">
                <Button asChild variant="outline" size="sm">
                  <a href={`#/reviewers/${encodeURIComponent(reviewerId)}/flashcards/edit/${encodeURIComponent(flashcard.id)}`} className="no-underline"><Pencil aria-hidden="true" />Edit</a>
                </Button>
                <Button variant="outline" size="sm" onClick={() => { setConfirmingId(flashcard.id); setDeleteError('') }} disabled={deletingId !== null}>
                  <Trash2 aria-hidden="true" />Delete
                </Button>
              </div>
            </CardHeader>
            <CardContent className="grid gap-2">
              <p className="text-sm leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]"><span className="font-medium">Back:</span> {flashcard.back}</p>
              {confirmingId === flashcard.id && <Alert role="alertdialog" aria-labelledby={`delete-flashcard-${flashcard.id}`}>
                <Trash2 aria-hidden="true" />
                <AlertDescription className="grid gap-3">
                  <p id={`delete-flashcard-${flashcard.id}`} className="text-foreground">Delete this flashcard? This can’t be undone.</p>
                  <div className="flex gap-2">
                    <Button variant="destructive" onClick={() => deleteFlashcard(flashcard.id)} disabled={deletingId !== null}>
                      {deletingId === flashcard.id ? 'Deleting…' : 'Yes, delete'}
                    </Button>
                    <Button variant="outline" onClick={() => setConfirmingId(null)} disabled={deletingId !== null}>Cancel</Button>
                  </div>
                </AlertDescription>
              </Alert>}
            </CardContent>
          </Card>
        </li>)}
      </ul>}
    </div>
  </section>
}
