import { useState, type FormEvent } from 'react'
import { Pencil, Play, Plus } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { Button } from '../../shared/ui/button'
import { Back, Empty, Failure, Field, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { DeleteAction } from '../../shared/ui/delete-action'
import type { Reviewer } from '../reviewers/types'
import type { Flashcard, ProgressSummary } from './types'
import { dateTime } from '../../shared/format'
import { DueOverview } from './DueOverview'
import { scopeQuery } from './query'
import { StudyPage } from './StudyPage'
import { ProgressPage } from './ProgressPage'

export function FlashcardsPage({ path }: { path: string[] }) {
  const [id, action] = path
  if (id === 'new') return <CardForm reviewerId={action} />
  if (id === 'study') return <StudyPage reviewerId={action} />
  if (id === 'progress') return <ProgressPage reviewerId={action} />
  if (id === 'due') return <StudyPage reviewerId={action} dueOnly />
  if (id === 'reviewer') return <CardList reviewerId={action} />
  if (id && action === 'edit') return <CardForm id={id} />
  return <CardList />
}

function CardList({ reviewerId }: { reviewerId?: string }) {
  const cards = useResource<Flashcard[]>(`/api/flashcards${reviewerId ? `?reviewerId=${encodeURIComponent(reviewerId)}` : ''}`)
  const reviewers = useResource<Reviewer[]>('/api/reviewers')
  const progress = useResource<ProgressSummary>('/api/flashcards/progress' + scopeQuery(reviewerId))
  const reviewer = reviewers.data?.find(item => item.id === reviewerId)
  return <>{reviewerId && <Back href={`#/reviewers/${reviewerId}`}>Back to reviewer</Back>}<PageTitle title="Flashcards" description={reviewer ? `Study cards for ${reviewer.title}.` : 'Turn important ideas into short questions and answers.'} actions={<><Button asChild variant="outline"><a href={`#/flashcards/progress${reviewerId ? `/${reviewerId}` : ''}`}>View progress</a></Button><Button asChild variant="outline"><a href={`#/flashcards/study${reviewerId ? `/${reviewerId}` : ''}`}><Play aria-hidden="true" />Study cards</a></Button><Button asChild><a href={`#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}><Plus aria-hidden="true" />New flashcard</a></Button></>} />{progress.data && <DueOverview summary={progress.data} reviewerId={reviewerId} />}{!!progress.error && <Failure error={progress.error} retry={progress.reload} />}{cards.loading ? <Loading label="Loading flashcards…" /> : cards.error ? <Failure error={cards.error} retry={cards.reload} /> : !cards.data?.length ? <Empty title="Make a little room for recall" action={<Button asChild><a href={`#/flashcards/new${reviewerId ? `/${reviewerId}` : ''}`}>Create a flashcard</a></Button>}>Write a prompt on the front and the answer on the back. Link each card to a reviewer.</Empty> : <div className="flashcard-list">{cards.data.map(card => <article className="paper-panel" key={card.id}><div className="section-heading"><p className="meta">{reviewers.data?.find(item => item.id === card.reviewerId)?.title || 'Reviewer flashcard'}</p><div className="actions"><Button asChild variant="ghost" size="small"><a href={`#/flashcards/${card.id}/edit`}><Pencil aria-hidden="true" />Edit</a></Button><DeleteAction name={card.front} remove={() => apiRequest(`/api/flashcards/${encodeURIComponent(card.id)}`, { method: 'DELETE' })} onDeleted={() => { cards.reload(); progress.reload() }} /></div></div><h2 className="card-prompt">{card.front}</h2><p className="meta card-schedule"><span className="pill">{card.status.toLowerCase()}</span>{card.due ? <span>Due now</span> : card.nextReviewAt && <span>Next review: {dateTime(card.nextReviewAt)}</span>}</p><details className="card-answer"><summary>Show answer</summary><p className="reading">{card.back}</p></details></article>)}</div>}</>
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

export function ReviewerFlashcards({ reviewerId }: { reviewerId: string }) {
  const cards = useResource<Flashcard[]>(`/api/flashcards?reviewerId=${encodeURIComponent(reviewerId)}`)
  return <section className="section-gap"><div className="section-heading"><h2>Flashcards</h2><Button asChild variant="outline"><a href={`#/flashcards/new/${reviewerId}`}><Plus aria-hidden="true" />New flashcard</a></Button></div>{cards.loading ? <Loading label="Loading flashcards…" /> : cards.error ? <Failure error={cards.error} retry={cards.reload} /> : <><p className="muted">{cards.data?.length ? `${cards.data.length} cards connected to this reviewer.` : 'Create focused prompts and answers from this study guide.'}</p><div className="actions linked-empty"><Button asChild variant="outline"><a href={`#/flashcards/reviewer/${reviewerId}`}>Manage cards</a></Button><Button asChild variant="outline"><a href={`#/flashcards/progress/${reviewerId}`}>View progress</a></Button>{!!cards.data?.length && <Button asChild><a href={`#/flashcards/study/${reviewerId}`}><Play aria-hidden="true" />Study cards</a></Button>}{!!cards.data?.length && <Button asChild variant="outline"><a href={`#/flashcards/due/${reviewerId}`}>Review due cards</a></Button>}</div></>}</section>
}
