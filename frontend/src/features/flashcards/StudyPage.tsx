import { useRef, useState } from 'react'
import { RotateCcw } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { dateTime } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Back, Empty, Failure, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { scopeQuery } from './query'
import type { Flashcard, ProgressSummary, Review } from './types'

export function StudyPage({ reviewerId, dueOnly = false }: { reviewerId?: string; dueOnly?: boolean }) {
  const cards = useResource<Flashcard[]>('/api/flashcards' + scopeQuery(reviewerId, dueOnly))
  const progress = useResource<ProgressSummary>(dueOnly ? '/api/flashcards/progress' + scopeQuery(reviewerId) : null)
  const back = `#/flashcards${reviewerId ? `/reviewer/${reviewerId}` : ''}`
  if (cards.loading || progress.loading) return <Loading label="Preparing your flashcards…" />
  const error = cards.error || progress.error
  const reload = () => { cards.reload(); progress.reload() }
  if (error) return <Failure error={error} retry={reload} />
  const caughtUp = dueOnly && !!progress.data?.totalCards
  return <>
    <Back href={back}>Back to flashcards</Back>
    <PageTitle title={dueOnly ? 'Review due cards' : 'Study flashcards'} description={dueOnly ? 'New cards and cards whose scheduled review time has arrived.' : 'Read the prompt, recall the answer, then reveal and rate your response.'} />
    {!cards.data?.length ? <Empty title={caughtUp ? 'All caught up' : 'No flashcards to study'} action={<Button asChild><a href={caughtUp ? `#/flashcards/study${reviewerId ? `/${reviewerId}` : ''}` : `#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}>{caughtUp ? 'Study all cards' : 'Create a flashcard'}</a></Button>}>
      {caughtUp ? progress.data?.nextDueAt ? `Your next scheduled review is ${dateTime(progress.data.nextDueAt)}. You can still practise your cards at any time.` : 'No cards are due right now. You can still practise at any time.' : 'Add a card to your reviewer to begin a study session.'}
    </Empty> : <StudySession cards={cards.data} restart={reload} back={back} dueOnly={dueOnly} progressHref={`#/flashcards/progress${reviewerId ? `/${reviewerId}` : ''}`} />}
  </>
}

function StudySession({ cards, restart, back, dueOnly, progressHref }: { cards: Flashcard[]; restart: () => void; back: string; dueOnly: boolean; progressHref: string }) {
  const [index, setIndex] = useState(0)
  const [revealed, setRevealed] = useState(false)
  const [reviews, setReviews] = useState<Review[]>([])
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const sending = useRef(false)
  async function rate(rating: number) {
    if (!revealed || sending.current) return
    sending.current = true; setPending(true); setError(null)
    try {
      const result = await apiRequest<Review>(`/api/flashcards/${encodeURIComponent(cards[index].id)}/review`, { method: 'POST', body: { rating } })
      if (result.flashcardId !== cards[index].id || result.rating !== rating) throw new Error('Could not confirm the saved review. Please try again.')
      setReviews(current => [...current, result]); setIndex(current => current + 1); setRevealed(false)
    } catch (failure) { setError(failure) } finally { sending.current = false; setPending(false) }
  }
  const lastReview = reviews.at(-1)
  const savedSchedule = lastReview && <p className="saved-review" role="status">Review saved. Next review: {dateTime(lastReview.nextReviewAt)} · {lastReview.intervalDays} {lastReview.intervalDays === 1 ? 'day' : 'days'}.</p>
  if (index >= cards.length) return <section className="study-complete">
    <h2>Session complete</h2><p>You reviewed {reviews.length} {reviews.length === 1 ? 'flashcard' : 'flashcards'}.</p>{savedSchedule}
    <p className="muted">Each rating updates the card’s schedule. Ratings 1–2 reset its recall streak; ratings 3–5 extend the interval as you learn.</p>
    <div className="actions"><Button onClick={restart}><RotateCcw aria-hidden="true" />{dueOnly ? 'Check due queue' : 'Study again'}</Button><Button asChild variant="outline"><a href={progressHref}>View progress</a></Button><Button asChild variant="ghost"><a href={back}>Back to flashcards</a></Button></div>
  </section>
  const card = cards[index]
  return <section className="study-session">
    <div className="study-progress"><span className="meta" role="status">Card {index + 1} of {cards.length}</span><progress value={index} max={cards.length} aria-label="Cards reviewed" /></div>
    {savedSchedule}<article className="paper-panel study-face"><p className="meta">Front</p><h2>{card.front}</h2></article>
    {revealed ? <article className="paper-panel study-face"><p className="meta">Answer</p><p className="reading">{card.back}</p></article> : <Button onClick={() => setRevealed(true)}>Reveal answer</Button>}
    {revealed && <fieldset disabled={pending}><legend className="field-label">How well did you recall it?</legend><div className="actions ratings">{['Again', 'Hard', 'Okay', 'Good', 'Easy'].map((label, value) => <Button key={label} variant="outline" aria-label={`Rate ${value + 1}: ${label}`} onClick={() => void rate(value + 1)}>{value + 1} · {label}</Button>)}</div></fieldset>}
    {!!error && <Notice error={error} />}{pending && <p role="status">Saving your review…</p>}
  </section>
}
