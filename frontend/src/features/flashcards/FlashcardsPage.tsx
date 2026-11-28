import { useRef, useState, type FormEvent } from 'react'
import { Pencil, Play, Plus, RotateCcw } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { Button } from '../../shared/ui/button'
import { Back, Empty, Failure, Field, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { DeleteAction } from '../../shared/ui/delete-action'
import type { Reviewer } from '../reviewers/types'
import type { Flashcard, Review } from './types'

export function FlashcardsPage({ path }: { path: string[] }) {
  const [id, action] = path
  if (id === 'new') return <CardForm reviewerId={action} />
  if (id === 'study') return <StudyPage reviewerId={action} />
  if (id === 'reviewer') return <CardList reviewerId={action} />
  if (id && action === 'edit') return <CardForm id={id} />
  return <CardList />
}

function CardList({ reviewerId }: { reviewerId?: string }) {
  const cards = useResource<Flashcard[]>(`/api/flashcards${reviewerId ? `?reviewerId=${encodeURIComponent(reviewerId)}` : ''}`)
  const reviewers = useResource<Reviewer[]>('/api/reviewers')
  const reviewer = reviewers.data?.find(item => item.id === reviewerId)
  return <>{reviewerId && <Back href={`#/reviewers/${reviewerId}`}>Back to reviewer</Back>}<PageTitle title="Flashcards" description={reviewer ? `Study cards for ${reviewer.title}.` : 'Turn important ideas into short questions and answers.'} actions={<><Button asChild variant="outline"><a href={`#/flashcards/study${reviewerId ? `/${reviewerId}` : ''}`}><Play aria-hidden="true" />Study cards</a></Button><Button asChild><a href={`#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}><Plus aria-hidden="true" />New flashcard</a></Button></>} />{cards.loading ? <Loading label="Loading flashcards…" /> : cards.error ? <Failure error={cards.error} retry={cards.reload} /> : !cards.data?.length ? <Empty title="Make a little room for recall" action={<Button asChild><a href={`#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}>Create a flashcard</a></Button>}>Write a prompt on the front and the answer on the back. Link each card to a reviewer.</Empty> : <div className="flashcard-list">{cards.data.map(card => <article className="paper-panel" key={card.id}><div className="section-heading"><p className="meta">{reviewers.data?.find(item => item.id === card.reviewerId)?.title || 'Reviewer flashcard'}</p><div className="actions"><Button asChild variant="ghost" size="small"><a href={`#/flashcards/${card.id}/edit`}><Pencil aria-hidden="true" />Edit</a></Button><DeleteAction name={card.front} remove={() => apiRequest(`/api/flashcards/${encodeURIComponent(card.id)}`, { method: 'DELETE' })} onDeleted={cards.reload} /></div></div><h2 className="card-prompt">{card.front}</h2><details className="card-answer"><summary>Show answer</summary><p className="reading">{card.back}</p></details></article>)}</div>}</>
}

function CardForm({ id, reviewerId }: { id?: string; reviewerId?: string }) {
  const card = useResource<Flashcard>(id ? `/api/flashcards/${encodeURIComponent(id)}` : null)
  const reviewers = useResource<Reviewer[]>('/api/reviewers')
  if (reviewers.loading || (id && card.loading)) return <Loading label="Loading flashcard editor…" />
  const error = card.error || reviewers.error
  if (error) return <Failure error={error} retry={() => { card.reload(); reviewers.reload() }} />
  return <><Back href="#/flashcards">All flashcards</Back><PageTitle title={id ? 'Edit flashcard' : 'New flashcard'} description="Keep each card focused on one idea." />{!reviewers.data?.length ? <Empty title="Start with a reviewer" action={<Button asChild><a href="#/reviewers/new">Create a reviewer</a></Button>}>Your flashcards connect to a study guide. Create a reviewer first.</Empty> : <CardFields initial={card.data} reviewers={reviewers.data} reviewerId={reviewerId} />}</>
}

function CardFields({ initial, reviewers, reviewerId }: { initial: Flashcard | null; reviewers: Reviewer[]; reviewerId?: string }) {
  const [selected, setSelected] = useState(initial?.reviewerId || (reviewers.some(item => item.id === reviewerId) ? reviewerId : '') || '')
  const [front, setFront] = useState(initial?.front || '')
  const [back, setBack] = useState(initial?.back || '')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  async function save(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (pending) return; if (!front.trim() || !back.trim() || !selected) { setError(new Error('Choose a reviewer and fill in both sides of the card.')); return } setPending(true); setError(null); try { await apiRequest(initial ? `/api/flashcards/${encodeURIComponent(initial.id)}` : '/api/flashcards', { method: initial ? 'PUT' : 'POST', body: { reviewerId: selected, front, back } }); window.location.hash = `#/flashcards/reviewer/${selected}` } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <form className="form-stack" onSubmit={save}><Field id="card-reviewer" label="Reviewer"><select className="select" id="card-reviewer" required value={selected} onChange={event => setSelected(event.target.value)}><option value="">Choose a reviewer</option>{reviewers.map(reviewer => <option key={reviewer.id} value={reviewer.id}>{reviewer.title}</option>)}</select></Field><Field id="card-front" label="Front — question or prompt"><textarea className="textarea question-text" id="card-front" required value={front} onChange={event => setFront(event.target.value)} /></Field><Field id="card-back" label="Back — answer"><textarea className="textarea question-text" id="card-back" required value={back} onChange={event => setBack(event.target.value)} /></Field>{!!error && <Notice error={error} />}<div className="actions"><Button type="submit" disabled={pending}>{pending ? 'Saving…' : 'Save flashcard'}</Button><Button asChild variant="outline"><a href="#/flashcards">Cancel</a></Button></div></form>
}

export function StudyPage({ reviewerId }: { reviewerId?: string }) {
  const cards = useResource<Flashcard[]>(`/api/flashcards${reviewerId ? `?reviewerId=${encodeURIComponent(reviewerId)}` : ''}`)
  const back = `#/flashcards${reviewerId ? `/reviewer/${reviewerId}` : ''}`
  if (cards.loading) return <Loading label="Preparing your flashcards…" />
  if (cards.error) return <Failure error={cards.error} retry={cards.reload} />
  return <><Back href={back}>Back to flashcards</Back><PageTitle title="Study flashcards" description="Read the prompt, recall the answer, then reveal and rate your response." />{!cards.data?.length ? <Empty title="No flashcards to study" action={<Button asChild><a href={`#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}>Create a flashcard</a></Button>}>Add a card to your reviewer to begin a study session.</Empty> : <StudySession cards={cards.data} restart={cards.reload} back={back} />}</>
}

function StudySession({ cards, restart, back }: { cards: Flashcard[]; restart: () => void; back: string }) {
  const [index, setIndex] = useState(0)
  const [revealed, setRevealed] = useState(false)
  const [reviews, setReviews] = useState<Review[]>([])
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const sending = useRef(false)
  async function rate(rating: number) { if (!revealed || sending.current) return; sending.current = true; setPending(true); setError(null); try { const result = await apiRequest<Review>(`/api/flashcards/${encodeURIComponent(cards[index].id)}/review`, { method: 'POST', body: { rating } }); if (result.flashcardId !== cards[index].id || result.rating !== rating) throw new Error('Could not confirm the saved review. Please try again.'); setReviews(current => [...current, result]); setIndex(current => current + 1); setRevealed(false) } catch (failure) { setError(failure) } finally { sending.current = false; setPending(false) } }
  if (index >= cards.length) return <section className="study-complete"><h2>Session complete</h2><p>You reviewed {reviews.length} {reviews.length === 1 ? 'flashcard' : 'flashcards'}.</p><div className="actions"><Button onClick={restart}><RotateCcw aria-hidden="true" />Study again</Button><Button asChild variant="outline"><a href={back}>Back to flashcards</a></Button></div></section>
  const card = cards[index]
  return <section className="study-session"><div className="study-progress"><span className="meta" role="status">Card {index + 1} of {cards.length}</span><progress value={index} max={cards.length} aria-label="Cards reviewed" /></div><article className="paper-panel study-face"><p className="meta">Front</p><h2>{card.front}</h2></article>{revealed ? <article className="paper-panel study-face"><p className="meta">Answer</p><p className="reading">{card.back}</p></article> : <Button onClick={() => setRevealed(true)}>Reveal answer</Button>}{revealed && <fieldset disabled={pending}><legend className="field-label">How well did you recall it?</legend><div className="actions ratings">{['Again', 'Hard', 'Okay', 'Good', 'Easy'].map((label, value) => <Button key={label} variant="outline" aria-label={`Rate ${value + 1}: ${label}`} onClick={() => void rate(value + 1)}>{value + 1} · {label}</Button>)}</div></fieldset>}{!!error && <Notice error={error} />}{pending && <p role="status">Saving your review…</p>}</section>
}

export function ReviewerFlashcards({ reviewerId }: { reviewerId: string }) {
  const cards = useResource<Flashcard[]>(`/api/flashcards?reviewerId=${encodeURIComponent(reviewerId)}`)
  return <section className="section-gap"><div className="section-heading"><h2>Flashcards</h2><Button asChild variant="outline"><a href={`#/flashcards/new/${reviewerId}`}><Plus aria-hidden="true" />New flashcard</a></Button></div>{cards.loading ? <Loading label="Loading flashcards…" /> : cards.error ? <Failure error={cards.error} retry={cards.reload} /> : <><p className="muted">{cards.data?.length ? `${cards.data.length} cards connected to this reviewer.` : 'Create focused prompts and answers from this study guide.'}</p><div className="actions linked-empty"><Button asChild variant="outline"><a href={`#/flashcards/reviewer/${reviewerId}`}>Manage cards</a></Button>{!!cards.data?.length && <Button asChild><a href={`#/flashcards/study/${reviewerId}`}><Play aria-hidden="true" />Study cards</a></Button>}</div></>}</section>
}
