import { useState, type FormEvent } from 'react'
import { ArrowRight, Pencil, Plus, Trash2 } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { dateOnly } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Input } from '../../shared/ui/input'
import { Back, Empty, Failure, Field, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { DeleteAction } from '../../shared/ui/delete-action'
import type { Quiz, QuizQuestion } from './types'
import type { Reviewer } from '../reviewers/types'

export function QuizzesPage({ path }: { path: string[] }) {
  const [id, action] = path
  if (id === 'new') return <QuizForm />
  if (id && action === 'edit') return <QuizForm id={id} />
  if (id) return <QuizDetail id={id} />
  return <QuizList />
}

function QuizList() {
  const resource = useResource<Quiz[]>('/api/quizzes')
  return <><PageTitle title="Quizzes" description="Turn your study guides into questions that put your knowledge to work." actions={<Button asChild><a href="#/quizzes/new"><Plus aria-hidden="true" />New quiz</a></Button>} />{resource.loading ? <Loading label="Loading quizzes…" /> : resource.error ? <Failure error={resource.error} retry={resource.reload} /> : !resource.data?.length ? <Empty title="Put your knowledge to the test" action={<Button asChild><a href="#/quizzes/new">Create a quiz</a></Button>}>Build a Practice quiz from your reviewers, with your own questions and answers.</Empty> : <ul className="resource-list">{resource.data.map(quiz => <li key={quiz.id}><a className="resource-row" href={`#/quizzes/${encodeURIComponent(quiz.id)}`}><div><h2>{quiz.title}</h2><div className="meta"><span>{quiz.questions.length} {quiz.questions.length === 1 ? 'question' : 'questions'}</span><span>{dateOnly(quiz.createdAt)}</span><span className="pill">Practice</span></div></div><ArrowRight size={18} aria-hidden="true" /></a></li>)}</ul>}</>
}

function QuizDetail({ id }: { id: string }) {
  const url = `/api/quizzes/${encodeURIComponent(id)}`
  const resource = useResource<Quiz>(url)
  if (resource.loading) return <Loading label="Opening quiz…" />
  if (resource.error || !resource.data) return <><Back href="#/quizzes">All quizzes</Back><Failure error={resource.error} retry={resource.reload} /></>
  const quiz = resource.data
  return <><Back href="#/quizzes">All quizzes</Back><PageTitle title={quiz.title} description={`${quiz.questions.length} multiple-choice ${quiz.questions.length === 1 ? 'question' : 'questions'} · Created ${dateOnly(quiz.createdAt)}`} actions={<><Button asChild variant="outline"><a href={`#/quizzes/${encodeURIComponent(id)}/edit`}><Pencil aria-hidden="true" />Edit</a></Button><DeleteAction name={quiz.title} remove={() => apiRequest(url, { method: 'DELETE' })} onDeleted={() => { window.location.hash = '#/quizzes' }} /></>} /><span className="pill">Practice</span>{quiz.hasAttempts && <p className="status-note">This quiz has saved attempts. Its questions are locked, and it cannot be deleted while those attempts exist. You can still edit its title and reviewer links.</p>}<section className="section-gap"><h2>Study from these reviewers</h2><ul className="linked-list">{quiz.reviewers.map(reviewer => <li key={reviewer.id}><a href={`#/reviewers/${encodeURIComponent(reviewer.id)}`}>{reviewer.title}<ArrowRight size={16} aria-hidden="true" /></a></li>)}</ul></section></>
}

function QuizForm({ id }: { id?: string }) {
  const resource = useResource<Quiz>(id ? `/api/quizzes/${encodeURIComponent(id)}` : null)
  const reviewers = useResource<Reviewer[]>('/api/reviewers')
  if (reviewers.loading || (id && resource.loading)) return <Loading label="Loading quiz editor…" />
  const error = resource.error || reviewers.error
  if (error) return <Failure error={error} retry={() => { resource.reload(); reviewers.reload() }} />
  return <><Back href={id ? `#/quizzes/${id}` : '#/quizzes'}>Back to quizzes</Back><PageTitle title={id ? 'Edit quiz' : 'New quiz'} description="Write 1–20 questions. Each question has four choices and one correct answer." />{!reviewers.data?.length ? <Empty title="Start with a reviewer" action={<Button asChild><a href="#/reviewers/new">Create a reviewer</a></Button>}>Link your quiz to at least one study guide.</Empty> : <QuizFields initial={resource.data} reviewers={reviewers.data} />}</>
}

type DraftQuestion = Omit<QuizQuestion, 'correctIndex'> & { correctIndex: number | null }
const blank = (): DraftQuestion => ({ text: '', choices: ['', '', '', ''], correctIndex: null })

function QuizFields({ initial, reviewers }: { initial: Quiz | null; reviewers: Reviewer[] }) {
  const [title, setTitle] = useState(initial?.title || '')
  const [selected, setSelected] = useState(initial?.reviewers.map(reviewer => reviewer.id) || [])
  const [questions, setQuestions] = useState<DraftQuestion[]>(initial?.questions || [blank()])
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const locked = !!initial?.hasAttempts
  function update(index: number, value: Partial<DraftQuestion>) { setQuestions(current => current.map((question, i) => i === index ? { ...question, ...value } : question)) }
  async function save(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (pending) return; if (!title.trim() || !selected.length || questions.some(question => !question.text.trim() || question.choices.some(choice => !choice.trim()) || question.correctIndex === null)) { setError(new Error('Add a title, select a reviewer, and complete every question with one correct choice.')); return } setPending(true); setError(null); try { const quiz = await apiRequest<Quiz>(initial ? `/api/quizzes/${encodeURIComponent(initial.id)}` : '/api/quizzes', { method: initial ? 'PUT' : 'POST', body: { title: title.trim(), mode: 'PRACTICE', reviewerIds: selected, questions: locked ? initial!.questions : questions.map(question => ({ ...question, text: question.text.trim(), choices: question.choices.map(choice => choice.trim()) })) } }); window.location.hash = `#/quizzes/${quiz.id}` } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <form className="form-stack" onSubmit={save}><Field id="quiz-title" label="Title"><Input id="quiz-title" required maxLength={150} value={title} onChange={event => setTitle(event.target.value)} /></Field><div className="meta"><span className="pill">Practice mode</span></div><fieldset><legend className="field-label">Source reviewers — choose at least one</legend><ul className="checkbox-list">{reviewers.map(reviewer => <li key={reviewer.id}><label><input type="checkbox" value={reviewer.id} checked={selected.includes(reviewer.id)} onChange={() => setSelected(current => current.includes(reviewer.id) ? current.filter(id => id !== reviewer.id) : [...current, reviewer.id])} /><span>{reviewer.title}</span></label></li>)}</ul></fieldset>
    {locked && <p className="status-note">Questions are locked because this quiz has saved attempts.</p>}
    <section><div className="section-heading"><h2>Questions</h2><span className="meta">{questions.length} / 20</span></div><div className="question-stack">{questions.map((question, index) => <fieldset className="question-editor paper-panel" key={index} disabled={locked || pending}><legend className="sr-only">Question {index + 1}</legend><div className="section-heading"><h3>Question {index + 1}</h3>{questions.length > 1 && !locked && <Button size="small" variant="ghost" aria-label={`Remove question ${index + 1}`} onClick={() => setQuestions(current => current.filter((_, i) => i !== index))}><Trash2 aria-hidden="true" />Remove</Button>}</div><Field id={`q${index}-text`} label="Question text"><textarea className="textarea question-text" id={`q${index}-text`} required maxLength={1000} value={question.text} onChange={event => update(index, { text: event.target.value })} /></Field><fieldset className="choice-editor"><legend className="field-label">Choose the correct answer</legend>{question.choices.map((choice, choiceIndex) => <div className="choice-row" key={choiceIndex}><input id={`q${index}-correct${choiceIndex}`} type="radio" name={`q${index}-correct`} required checked={question.correctIndex === choiceIndex} aria-label={`Choice ${String.fromCharCode(65 + choiceIndex)} is correct for question ${index + 1}`} onChange={() => update(index, { correctIndex: choiceIndex })} /><Input id={`q${index}-choice${choiceIndex}`} aria-label={`Question ${index + 1}, choice ${String.fromCharCode(65 + choiceIndex)}`} required maxLength={300} value={choice} onChange={event => update(index, { choices: question.choices.map((value, i) => i === choiceIndex ? event.target.value : value) })} /></div>)}</fieldset></fieldset>)}</div>{!locked && questions.length < 20 && <Button className="add-question" variant="outline" onClick={() => setQuestions(current => [...current, blank()])}><Plus aria-hidden="true" />Add question</Button>}</section>{!!error && <Notice error={error} />}<div className="actions"><Button type="submit" disabled={pending}>{pending ? 'Saving…' : 'Save quiz'}</Button><Button asChild variant="outline"><a href={initial ? `#/quizzes/${initial.id}` : '#/quizzes'}>Cancel</a></Button></div></form>
}
