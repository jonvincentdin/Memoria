import { useState } from 'react'
import { CalendarClock, CircleAlert, History, Pencil, Plus, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatInterval, formatShortDate, RATING_LABELS, STATUS_LABELS } from './format'

export default function FlashcardSection({ reviewerId }) {
  const encodedId = encodeURIComponent(reviewerId)
  const [flashcards, retry] = useApiData(`/api/flashcards?reviewerId=${encodedId}`)
  const [progress, retryProgress] = useApiData(`/api/flashcards/progress?reviewerId=${encodedId}`)
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
      retryProgress()
    } catch (error) {
      setDeleteError(errorMessage(error))
      setConfirmingId(null)
      setDeletingId(null)
    }
  }

  const summary = progress.status === 'ready' ? progress.data : null
  const dueCount = summary?.dueCards ?? 0
  const masteredPct = summary && summary.totalCards > 0
    ? Math.round((summary.masteredCards / summary.totalCards) * 100)
    : 0
  const hasCards = flashcards.status === 'ready' && flashcards.data.length > 0

  return <section className="mt-8" aria-labelledby="flashcards-heading">
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h2 id="flashcards-heading" className="font-heading text-2xl">Flashcards</h2>
        <p className="mt-1 text-sm text-muted-foreground">Cards and spaced-repetition schedule for this reviewer.</p>
      </div>
      <div className="flex flex-wrap gap-2">
        {hasCards && dueCount > 0 && <Button asChild>
          <a href={`#/reviewers/${encodedId}/study/due`} className="no-underline">Study due ({dueCount})</a>
        </Button>}
        {hasCards && <Button asChild variant="outline">
          <a href={`#/reviewers/${encodedId}/study`} className="no-underline">Study all</a>
        </Button>}
        {hasCards && <Button asChild variant="outline">
          <a href={`#/reviewers/${encodedId}/progress`} className="no-underline"><History aria-hidden="true" />Progress &amp; history</a>
        </Button>}
        <Button asChild variant={hasCards && dueCount > 0 ? 'outline' : 'default'}>
          <a href={`#/reviewers/${encodedId}/flashcards/new`} className="no-underline"><Plus aria-hidden="true" />New flashcard</a>
        </Button>
      </div>
    </div>

    {summary && summary.totalCards > 0 && <Card className="mt-4">
      <CardContent className="grid gap-3 py-4">
        <div className="flex flex-wrap items-center justify-between gap-2 text-sm">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant={summary.dueCards > 0 ? 'default' : 'secondary'}>
              {summary.dueCards > 0 ? `${summary.dueCards} due now` : 'All caught up'}
            </Badge>
            <span className="text-muted-foreground">
              {summary.masteredCards} mastered · {summary.learningCards} learning · {summary.newCards} new
            </span>
          </div>
          <span className="text-xs text-muted-foreground">
            {summary.totalReviews === 0
              ? 'No reviews logged yet'
              : `${summary.totalReviews} ${summary.totalReviews === 1 ? 'review' : 'reviews'} · Avg ${summary.averageRating.toFixed(1)}/5`}
          </span>
        </div>
        <div className="grid gap-1.5">
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <span>Mastery progress</span>
            <span>{masteredPct}% ({summary.masteredCards} of {summary.totalCards})</span>
          </div>
          <Progress value={masteredPct} aria-label={`Mastery progress: ${masteredPct}%`} />
        </div>
        {summary.dueCards === 0 && summary.nextDueAt && <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <CalendarClock className="size-3.5 shrink-0" aria-hidden="true" />
          Next scheduled review on {formatShortDate(summary.nextDueAt)}.
        </p>}
      </CardContent>
    </Card>}

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
          <div><Button asChild variant="outline"><a href={`#/reviewers/${encodedId}/flashcards/new`} className="no-underline"><Plus aria-hidden="true" />Create flashcard</a></Button></div>
        </CardContent>
      </Card>}

      {hasCards && <ul className="grid list-none gap-3 p-0">
        {flashcards.data.map((flashcard) => <li key={flashcard.id}>
          <Card>
            <CardHeader className="flex flex-row items-start justify-between gap-4">
              <div className="min-w-0 grid gap-1.5">
                <CardTitle className="min-w-0 text-base font-medium [overflow-wrap:anywhere]">{flashcard.front}</CardTitle>
                <div className="flex flex-wrap items-center gap-1.5">
                  <Badge variant={flashcard.due ? 'default' : flashcard.status === 'MASTERED' ? 'secondary' : 'outline'}>
                    {flashcard.due ? 'Due now' : STATUS_LABELS[flashcard.status] || 'Scheduled'}
                  </Badge>
                  {flashcard.status && flashcard.due && <Badge variant="outline">{STATUS_LABELS[flashcard.status]}</Badge>}
                </div>
              </div>
              <div className="flex shrink-0 gap-2">
                <Button asChild variant="outline" size="sm">
                  <a href={`#/reviewers/${encodedId}/flashcards/edit/${encodeURIComponent(flashcard.id)}`} className="no-underline"><Pencil aria-hidden="true" />Edit</a>
                </Button>
                <Button variant="outline" size="sm" onClick={() => { setConfirmingId(flashcard.id); setDeleteError('') }} disabled={deletingId !== null}>
                  <Trash2 aria-hidden="true" />Delete
                </Button>
              </div>
            </CardHeader>
            <CardContent className="grid gap-2">
              <p className="text-sm leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]"><span className="font-medium">Back:</span> {flashcard.back}</p>
              <p className="text-xs text-muted-foreground">
                {flashcard.lastReviewedAt
                  ? `Last rated ${flashcard.lastRating}/5 (${RATING_LABELS[flashcard.lastRating] || ''}) · Interval ${formatInterval(flashcard.intervalDays)} · ${flashcard.due ? 'Due now' : `Next review ${formatShortDate(flashcard.nextReviewAt)}`}`
                  : 'New card · Ready for first review'}
              </p>
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
