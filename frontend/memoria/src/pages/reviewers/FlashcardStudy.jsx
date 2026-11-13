import { useEffect, useRef, useState } from 'react'
import { ArrowLeft, CircleAlert } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'

const RATINGS = [
  { value: 1, label: 'Not recalled' },
  { value: 2, label: 'A little' },
  { value: 3, label: 'Mostly' },
  { value: 4, label: 'Correctly' },
  { value: 5, label: 'Easily' },
]

export default function FlashcardStudy({ reviewerId }) {
  const [flashcards, retry] = useApiData(`/api/flashcards?reviewerId=${encodeURIComponent(reviewerId)}`)
  const [index, setIndex] = useState(0)
  const [revealed, setRevealed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [reviewError, setReviewError] = useState('')
  const submittingRef = useRef(false)

  useEffect(() => { document.title = 'Study flashcards · Memoria' }, [])

  const backHref = `#/reviewers/${encodeURIComponent(reviewerId)}`

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
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">Study flashcards</h1>
    <p className="mt-6 text-muted-foreground" aria-live="polite">Loading flashcards…</p>
  </section>

  if (flashcards.status === 'error') return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">Study flashcards</h1>
    <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {errorMessage(flashcards.error)}
        <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
      </AlertDescription>
    </Alert>
  </section>

  if (flashcards.data.length === 0) return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">Study flashcards</h1>
    <Card className="mt-6">
      <CardContent className="grid gap-3 py-8">
        <p className="font-medium">No flashcards to study</p>
        <p className="text-sm text-muted-foreground">Create a flashcard for this reviewer to start a study session.</p>
        <div><Button asChild><a href={`${backHref}/flashcards/new`} className="no-underline">Create flashcard</a></Button></div>
      </CardContent>
    </Card>
  </section>

  if (index >= flashcards.data.length) return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">Session complete</h1>
    <p className="mt-3 text-muted-foreground" role="status">You reviewed all {flashcards.data.length} flashcards in this session.</p>
    <div className="mt-6 flex flex-wrap gap-3">
      <Button asChild><a href={backHref} className="no-underline">Return to reviewer</a></Button>
      <Button asChild variant="outline"><a href={`#/reviewers/${encodeURIComponent(reviewerId)}/study`} className="no-underline">Study again</a></Button>
    </div>
  </section>

  const flashcard = flashcards.data[index]
  return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <div className="mt-4 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="font-heading text-4xl">Study flashcards</h1>
        <p className="mt-2 text-sm text-muted-foreground" aria-live="polite">Card {index + 1} of {flashcards.data.length}</p>
      </div>
    </div>

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
      <legend className="mb-3 text-sm font-medium">How well did you recall it?</legend>
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
