import { useEffect } from 'react'
import { ArrowLeft, CalendarClock, CircleAlert, History } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDateTime, formatInterval, formatShortDate, RATING_LABELS } from './format'

export default function FlashcardProgress({ reviewerId }) {
  const encodedId = reviewerId ? encodeURIComponent(reviewerId) : null
  const query = encodedId ? `?reviewerId=${encodedId}` : ''
  const [progress, retryProgress] = useApiData(`/api/flashcards/progress${query}`)
  const [reviews, retryReviews] = useApiData(`/api/flashcards/reviews${query}`)

  const title = encodedId ? 'Reviewer flashcard progress' : 'Flashcard progress'
  useEffect(() => { document.title = `${title} · Memoria` }, [title])

  const backHref = encodedId ? `#/reviewers/${encodedId}` : '#/reviewers'
  const backLabel = encodedId ? 'Back to reviewer' : 'All reviewers'
  const studyDueHref = encodedId ? `#/reviewers/${encodedId}/study/due` : '#/reviewers/study/due'
  const studyAllHref = encodedId ? `#/reviewers/${encodedId}/study` : null

  const summary = progress.status === 'ready' ? progress.data : null
  const masteredPct = summary && summary.totalCards > 0
    ? Math.round((summary.masteredCards / summary.totalCards) * 100)
    : 0

  return <section className="mx-auto max-w-4xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />{backLabel}</a>

    <div className="mt-4 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-heading text-4xl">{title}</h1>
        <p className="mt-2 text-muted-foreground">Spaced-repetition status and recorded study reviews.</p>
      </div>
      {summary && <div className="flex flex-wrap gap-2">
        {summary.dueCards > 0 && <Button asChild>
          <a href={studyDueHref} className="no-underline">Study due ({summary.dueCards})</a>
        </Button>}
        {studyAllHref && summary.totalCards > 0 && <Button asChild variant="outline">
          <a href={studyAllHref} className="no-underline">Study all</a>
        </Button>}
      </div>}
    </div>

    <div className="mt-6" aria-live="polite">
      {progress.status === 'loading' && <p className="text-sm text-muted-foreground">Loading progress summary…</p>}

      {progress.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(progress.error)}
          <Button variant="outline" size="sm" onClick={retryProgress}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {summary && <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Due now</CardDescription>
            <CardTitle className="font-heading text-3xl font-normal">{summary.dueCards}</CardTitle>
          </CardHeader>
          <CardContent className="text-xs text-muted-foreground">
            {summary.dueCards > 0
              ? `Ready to study out of ${summary.totalCards} ${summary.totalCards === 1 ? 'card' : 'cards'}`
              : summary.nextDueAt
                ? `Next due ${formatShortDate(summary.nextDueAt)}`
                : summary.totalCards === 0
                  ? 'No flashcards yet'
                  : 'All caught up'}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Mastered</CardDescription>
            <CardTitle className="font-heading text-3xl font-normal">{summary.masteredCards} / {summary.totalCards}</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-2 text-xs text-muted-foreground">
            <Progress value={masteredPct} aria-label={`Mastery progress: ${masteredPct}%`} />
            <span>{summary.learningCards} learning · {summary.newCards} new</span>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Reviews completed</CardDescription>
            <CardTitle className="font-heading text-3xl font-normal">{summary.totalReviews}</CardTitle>
          </CardHeader>
          <CardContent className="text-xs text-muted-foreground">
            {summary.reviewsToday} {summary.reviewsToday === 1 ? 'review' : 'reviews'} today (UTC)
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Average recall</CardDescription>
            <CardTitle className="font-heading text-3xl font-normal">
              {summary.averageRating == null ? '—' : `${summary.averageRating.toFixed(1)} / 5`}
            </CardTitle>
          </CardHeader>
          <CardContent className="text-xs text-muted-foreground">
            {summary.lastReviewedAt ? `Last reviewed ${formatShortDate(summary.lastReviewedAt)}` : 'No reviews yet'}
          </CardContent>
        </Card>
      </div>}
    </div>

    <section className="mt-10" aria-labelledby="review-history-heading">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 id="review-history-heading" className="font-heading text-2xl">Review history</h2>
        {reviews.status === 'ready' && reviews.data.length > 0 && <span className="text-xs text-muted-foreground">Newest first</span>}
      </div>

      <div className="mt-4" aria-live="polite">
        {reviews.status === 'loading' && <p className="text-sm text-muted-foreground">Loading review history…</p>}

        {reviews.status === 'error' && <Alert variant="destructive">
          <CircleAlert aria-hidden="true" />
          <AlertDescription className="flex flex-wrap items-center gap-3">
            {errorMessage(reviews.error)}
            <Button variant="outline" size="sm" onClick={retryReviews}>Try again</Button>
          </AlertDescription>
        </Alert>}

        {reviews.status === 'ready' && reviews.data.length === 0 && <Card>
          <CardContent className="grid gap-3 py-8">
            <span className="grid size-10 place-items-center rounded-lg bg-secondary text-secondary-foreground"><History className="size-5" aria-hidden="true" /></span>
            <p className="font-medium">No reviews recorded yet</p>
            <p className="text-sm text-muted-foreground">Rate your recall during a flashcard study session to start tracking progress and next review dates.</p>
            {summary && summary.dueCards > 0 && <div>
              <Button asChild><a href={studyDueHref} className="no-underline">Start studying due cards</a></Button>
            </div>}
          </CardContent>
        </Card>}

        {reviews.status === 'ready' && reviews.data.length > 0 && <ul className="grid list-none gap-3 p-0">
          {reviews.data.map((entry) => <li key={entry.id}>
            <Card>
              <CardContent className="flex flex-wrap items-start justify-between gap-4 py-4">
                <div className="min-w-0 grid gap-1.5">
                  <p className="font-medium [overflow-wrap:anywhere]">{entry.front}</p>
                  <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                    <span>Reviewed {formatDateTime(entry.reviewedAt)}</span>
                    <span>·</span>
                    <span className="inline-flex items-center gap-1">
                      <CalendarClock className="size-3.5" aria-hidden="true" />
                      Next due {formatShortDate(entry.nextReviewAt)} ({formatInterval(entry.intervalDays)})
                    </span>
                    {!encodedId && <>
                      <span>·</span>
                      <a href={`#/reviewers/${encodeURIComponent(entry.reviewerId)}`} className="text-primary no-underline hover:underline">{entry.reviewerTitle}</a>
                    </>}
                  </div>
                </div>
                <Badge variant={entry.rating >= 3 ? 'secondary' : 'destructive'}>
                  {entry.rating} · {RATING_LABELS[entry.rating] || 'Rated'}
                </Badge>
              </CardContent>
            </Card>
          </li>)}
        </ul>}
      </div>
    </section>
  </section>
}
