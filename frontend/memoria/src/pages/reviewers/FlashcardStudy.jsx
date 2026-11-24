import { useEffect, useRef, useState } from 'react'
import { ArrowLeft, CalendarCheck, CircleAlert, History } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatInterval, formatShortDate, RATING_LABELS, STATUS_LABELS } from './format'

const RATINGS = [1, 2, 3, 4, 5].map((value) => ({ value, label: RATING_LABELS[value] }))

export default function FlashcardStudy({ reviewerId, dueOnly = false }) {
  const encodedId = reviewerId ? encodeURIComponent(reviewerId) : null
  const endpoint = encodedId
    ? `/api/flashcards?reviewerId=${encodedId}${dueOnly ? '&dueOnly=true' : ''}`
    : '/api/flashcards?dueOnly=true'
  const progressEndpoint = encodedId
    ? `/api/flashcards/progress?reviewerId=${encodedId}`
    : '/api/flashcards/progress'

  const [flashcards, retry] = useApiData(endpoint)
  const [progress, retryProgress] = useApiData(progressEndpoint)
  const [index, setIndex] = useState(0)
  const [revealed, setRevealed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [reviewError, setReviewError] = useState('')
  const [sessionReviews, setSessionReviews] = useState([])
  const [lastScheduled, setLastScheduled] = useState(null)
  const submittingRef = useRef(false)

  const heading = dueOnly ? 'Study due flashcards' : 'Study flashcards'
  useEffect(() => { document.title = `${heading} · Memoria` }, [heading])

  const backHref = encodedId ? `#/reviewers/${encodedId}` : '#/reviewers'
  const backLabel = encodedId ? 'Back to reviewer' : 'All reviewers'
  const progressHref = encodedId ? `#/reviewers/${encodedId}/progress` : '#/reviewers/progress'
  const studyAllHref = encodedId ? `#/reviewers/${encodedId}/study` : '#/reviewers'

  function resetSession() {
    setIndex(0)
    setRevealed(false)
    setReviewError('')
    setSessionReviews([])
    setLastScheduled(null)
    retry()
    retryProgress()
  }

  async function submitRating(rating) {
    const current = flashcards.status === 'ready' ? flashcards.data[index] : null
    if (!current || !revealed || submittingRef.current) return

    submittingRef.current = true
    setSubmitting(true)
    setReviewError('')
    try {
      const result = await apiRequest(`/api/flashcards/${encodeURIComponent(current.id)}/review`, {
        method: 'POST',
        body: { rating },
      })
      if (result?.flashcardId !== current.id || result.rating !== rating) {
        setReviewError('The review could not be confirmed. Please try again.')
        return
      }
      setSessionReviews((prev) => [...prev, result])
      setLastScheduled({
        rating: result.rating,
        intervalDays: result.intervalDays,
        nextReviewAt: result.nextReviewAt,
      })
      setRevealed(false)
      setIndex((currentIndex) => currentIndex + 1)
    } catch (error) {
      setReviewError(errorMessage(error))
    } finally {
      submittingRef.current = false
      setSubmitting(false)
    }
  }

  if (flashcards.status === 'loading') return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>
    <h1 className="mt-4 font-heading text-4xl">{heading}</h1>
    <p className="mt-6 text-muted-foreground" aria-live="polite">Loading flashcards…</p>
  </section>

  if (flashcards.status === 'error') return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>
    <h1 className="mt-4 font-heading text-4xl">{heading}</h1>
    <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {errorMessage(flashcards.error)}
        <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
      </AlertDescription>
    </Alert>
  </section>

  if (flashcards.data.length === 0) {
    const summary = progress.status === 'ready' ? progress.data : null
    const hasAnyCards = (summary?.totalCards ?? 0) > 0
    return <section className="mx-auto max-w-3xl">
      <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>
      <h1 className="mt-4 font-heading text-4xl">{heading}</h1>
      <Card className="mt-6">
        <CardContent className="grid gap-3 py-8">
          <p className="font-medium">{dueOnly && hasAnyCards ? 'All caught up' : 'No flashcards to study'}</p>
          <p className="text-sm text-muted-foreground">
            {dueOnly && hasAnyCards
              ? summary.nextDueAt
                ? `No cards are due right now. Your next scheduled review is on ${formatShortDate(summary.nextDueAt)}.`
                : 'No cards are due right now.'
              : encodedId
                ? 'Create a flashcard for this reviewer to start a study session.'
                : 'Create flashcards inside any reviewer to start studying.'}
          </p>
          <div className="flex flex-wrap gap-2">
            {dueOnly && hasAnyCards && encodedId && <Button asChild>
              <a href={studyAllHref} className="no-underline">Study all cards anyway</a>
            </Button>}
            {dueOnly && hasAnyCards && <Button asChild variant={encodedId ? 'outline' : 'default'}>
              <a href={progressHref} className="no-underline"><History aria-hidden="true" />View progress &amp; history</a>
            </Button>}
            {!hasAnyCards && encodedId && <Button asChild>
              <a href={`${backHref}/flashcards/new`} className="no-underline">Create flashcard</a>
            </Button>}
            {!hasAnyCards && !encodedId && <Button asChild>
              <a href="#/reviewers" className="no-underline">Browse reviewers</a>
            </Button>}
          </div>
        </CardContent>
      </Card>
    </section>
  }

  if (index >= flashcards.data.length) {
    const recalledCount = sessionReviews.filter((r) => r.rating >= 3).length
    const needsReviewCount = sessionReviews.length - recalledCount
    const earliestNext = sessionReviews
      .map((r) => r.nextReviewAt)
      .filter(Boolean)
      .sort()[0]

    return <section className="mx-auto max-w-3xl">
      <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>
      <h1 className="mt-4 font-heading text-4xl">Session complete</h1>
      <p className="mt-3 text-muted-foreground" role="status">
        You reviewed {sessionReviews.length} {sessionReviews.length === 1 ? 'flashcard' : 'flashcards'} and updated their next review dates.
      </p>

      <div className="mt-6 grid gap-3 sm:grid-cols-3">
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Recalled (3–5)</CardTitle></CardHeader>
          <CardContent><p className="font-heading text-3xl">{recalledCount}</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Needs review (1–2)</CardTitle></CardHeader>
          <CardContent><p className="font-heading text-3xl">{needsReviewCount}</p></CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm font-medium text-muted-foreground">Earliest next review</CardTitle></CardHeader>
          <CardContent><p className="text-base font-medium">{earliestNext ? formatShortDate(earliestNext) : '—'}</p></CardContent>
        </Card>
      </div>

      <div className="mt-6 flex flex-wrap gap-3">
        <Button asChild><a href={backHref} className="no-underline">{backLabel}</a></Button>
        <Button asChild variant="outline"><a href={progressHref} className="no-underline"><History aria-hidden="true" />View progress &amp; history</a></Button>
        {!dueOnly && <Button type="button" variant="outline" onClick={resetSession}>Study again</Button>}
        {dueOnly && encodedId && <Button asChild variant="outline"><a href={studyAllHref} className="no-underline">Study all cards</a></Button>}
      </div>
    </section>
  }

  const flashcard = flashcards.data[index]
  return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>
    <div className="mt-4 flex flex-wrap items-end justify-between gap-3">
      <div>
        <div className="flex flex-wrap items-center gap-2">
          <h1 className="font-heading text-4xl">{heading}</h1>
          <Badge variant={dueOnly ? 'default' : 'secondary'}>{dueOnly ? 'Due queue' : 'All cards'}</Badge>
        </div>
        <p className="mt-2 text-sm text-muted-foreground" aria-live="polite">
          Card {index + 1} of {flashcards.data.length}
          {flashcard.status ? ` · ${STATUS_LABELS[flashcard.status]}` : ''}
          {flashcard.intervalDays > 0 ? ` · Current interval ${formatInterval(flashcard.intervalDays)}` : ''}
        </p>
      </div>
      {encodedId && <Button asChild variant="outline" size="sm">
        <a href={dueOnly ? studyAllHref : `#/reviewers/${encodedId}/study/due`} className="no-underline">
          {dueOnly ? 'Switch to all cards' : 'Switch to due only'}
        </a>
      </Button>}
    </div>

    {lastScheduled && <p className="mt-4 inline-flex items-center gap-1.5 text-xs text-muted-foreground" role="status">
      <CalendarCheck className="size-3.5 text-primary" aria-hidden="true" />
      Previous card rated {lastScheduled.rating}/5 — scheduled for {formatShortDate(lastScheduled.nextReviewAt)} ({formatInterval(lastScheduled.intervalDays)}).
    </p>}

    <Card className="mt-6">
      <CardHeader><CardTitle className="text-sm font-medium text-muted-foreground">Front</CardTitle></CardHeader>
      <CardContent><p className="text-lg leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]">{flashcard.front}</p></CardContent>
    </Card>

    {revealed
      ? <Card className="mt-4">
        <CardHeader><CardTitle className="text-sm font-medium text-muted-foreground">Back</CardTitle></CardHeader>
        <CardContent><p className="text-lg leading-relaxed whitespace-pre-wrap [overflow-wrap:anywhere]">{flashcard.back}</p></CardContent>
      </Card>
      : <Button className="mt-4" size="lg" onClick={() => setRevealed(true)}>Reveal answer</Button>}

    {revealed && <fieldset className="mt-6 grid gap-3" disabled={submitting}>
      <legend className="mb-3 text-sm font-medium">How well did you recall it? Your rating schedules the next review date.</legend>
      <div className="flex flex-wrap gap-2">
        {RATINGS.map(({ value, label }) => <Button key={value} type="button" variant="outline" onClick={() => submitRating(value)} disabled={submitting} aria-label={`Rate ${value}: ${label}`}>
          {value} · {label}
        </Button>)}
      </div>
    </fieldset>}

    {reviewError && <Alert variant="destructive" className="mt-4" role="alert">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{reviewError}</AlertDescription>
    </Alert>}

    {submitting && <p className="mt-3 text-sm text-muted-foreground" aria-live="polite">Saving review…</p>}
  </section>
}
