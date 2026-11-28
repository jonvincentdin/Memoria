import { useRef, useState, type FormEvent } from 'react'
import { RotateCcw, Send } from 'lucide-react'
import { apiRequest, ApiError } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { dateTime } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Back, Empty, Failure, Loading, Notice, PageTitle } from '../../shared/ui/common'
import type { Attempt, TakingQuiz } from './types'

export function QuizTaking({ id }: { id: string }) {
  const resource = useResource<TakingQuiz>(`/api/quizzes/${encodeURIComponent(id)}/take`)
  if (resource.loading) return <Loading label="Preparing your quiz…" />
  if (resource.error || !resource.data) return <Failure error={resource.error} retry={resource.reload} />
  return <><Back href={`#/quizzes/${id}`}>Back to quiz</Back><PageTitle title={resource.data.title} description="Choose one answer per question. Your score will be calculated when you submit." /><TakingForm id={id} quiz={resource.data} /></>
}

function TakingForm({ id, quiz }: { id: string; quiz: TakingQuiz }) {
  const [answers, setAnswers] = useState<Record<number, number>>({})
  const [confirm, setConfirm] = useState(false)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const sending = useRef(false)
  const answered = Object.keys(answers).length
  async function send() { if (sending.current) return; sending.current = true; setPending(true); setError(null); try { const result = await apiRequest<Attempt>(`/api/quizzes/${encodeURIComponent(id)}/attempts`, { method: 'POST', body: { answers: Object.entries(answers).map(([questionIndex, choiceIndex]) => ({ questionIndex: Number(questionIndex), choiceIndex })) } }); window.location.hash = `#/quizzes/${id}/results/${result.id}` } catch (failure) { setError(failure); sending.current = false; setPending(false); setConfirm(false) } }
  function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (answered < quiz.questions.length) setConfirm(true); else void send() }
  return <form className="form-stack" onSubmit={submit}><p className="meta" role="status">{answered} of {quiz.questions.length} answered</p><div className="question-stack">{quiz.questions.map((question, index) => <fieldset className="paper-panel take-question" key={index} disabled={pending}><legend className="sr-only">Question {index + 1}</legend><p className="meta">Question {index + 1}</p><h2>{question.text}</h2><div className="answer-options">{question.choices.map((choice, choiceIndex) => <label key={choiceIndex}><input type="radio" name={`answer-${index}`} value={choiceIndex} checked={answers[index] === choiceIndex} onChange={() => { setAnswers(current => ({ ...current, [index]: choiceIndex })); setConfirm(false) }} /><span>{choice}</span></label>)}</div>{answers[index] !== undefined && <Button size="small" variant="ghost" onClick={() => { setAnswers(current => { const next = { ...current }; delete next[index]; return next }); setConfirm(false) }}>Clear answer<span className="sr-only"> for question {index + 1}</span></Button>}</fieldset>)}</div>{!!error && <Notice error={error} />}{confirm ? <div className="status-note" role="status"><p>{quiz.questions.length - answered} unanswered {quiz.questions.length - answered === 1 ? 'question will' : 'questions will'} count as incorrect. Submit your quiz?</p><div className="actions confirm-actions"><Button disabled={pending} onClick={() => void send()}>{pending ? 'Submitting…' : 'Submit anyway'}</Button><Button variant="outline" disabled={pending} onClick={() => setConfirm(false)}>Keep answering</Button></div></div> : <Button type="submit" disabled={pending}>{pending ? 'Submitting…' : 'Submit quiz'}<Send aria-hidden="true" /></Button>}</form>
}

export function QuizResult({ quizId, attemptId }: { quizId: string; attemptId: string }) {
  const resource = useResource<Attempt>(`/api/attempts/${encodeURIComponent(attemptId)}`)
  if (resource.loading) return <Loading label="Loading saved result…" />
  if (resource.error || !resource.data || resource.data.quizId !== quizId) return <><Back href="#/quizzes">All quizzes</Back><Failure error={resource.error || new ApiError('This result does not belong to this quiz.', 404)} retry={resource.reload} /></>
  const result = resource.data
  return <><Back href={`#/quizzes/${encodeURIComponent(quizId)}`}>Back to quiz</Back><PageTitle title="Your quiz result" description={result.quizTitle} /><section className="quiz-result"><p className="score"><span>{result.score}</span> / {result.totalQuestions}</p><p className="result-percent">{Math.round(result.percentage)}% correct</p><p className="muted">Saved {dateTime(result.completedAt)}</p><div className="actions"><Button asChild><a href={`#/quizzes/${encodeURIComponent(quizId)}/take`}><RotateCcw aria-hidden="true" />Take again</a></Button><Button asChild variant="outline"><a href="#/quizzes">All quizzes</a></Button></div></section></>
}

export function SavedAttempts({ quizId }: { quizId: string }) {
  const resource = useResource<Attempt[]>(`/api/attempts?quizId=${encodeURIComponent(quizId)}`)
  return <section className="section-gap"><h2>Saved attempts</h2>{resource.loading ? <Loading label="Loading attempts…" /> : resource.error ? <Failure error={resource.error} retry={resource.reload} /> : !resource.data?.length ? <Empty title="A fresh start">Take this quiz to save your first result.</Empty> : <ul className="linked-list">{resource.data.map(attempt => <li key={attempt.id}><a href={`#/quizzes/${encodeURIComponent(quizId)}/results/${encodeURIComponent(attempt.id)}`}><span>{dateTime(attempt.completedAt)}</span><strong className="tabular">{attempt.score} / {attempt.totalQuestions} · {Math.round(attempt.percentage)}%</strong></a></li>)}</ul>}</section>
}
