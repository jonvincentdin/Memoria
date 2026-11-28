import { useEffect, useRef, useState } from 'react'
import { ArrowLeft, CircleAlert, CircleHelp } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { LoadError } from './QuizDetail'

const LETTERS = ['A', 'B', 'C', 'D']

// Taking screen. Loads the take endpoint, which has no answer keys, and submits the attempt once.
export default function QuizTake({ id }) {
  const [quiz, retry] = useApiData(`/api/quizzes/${id}/take`)
  const backLink = <a href={`#/quizzes/${id}`} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to quiz</a>

  const title = quiz.status === 'ready' ? quiz.data.title : 'Take quiz'
  useEffect(() => { document.title = `${title} · Memoria` }, [title])

  if (quiz.status === 'loading') return <section className="mx-auto max-w-3xl">{backLink}<p className="mt-6 text-muted-foreground" aria-live="polite">Loading quiz…</p></section>
  if (quiz.status === 'error') return <section className="mx-auto max-w-3xl">{backLink}<LoadError state={quiz} retry={retry} /></section>

  return <section className="mx-auto max-w-3xl">
    {backLink}
    <h1 className="mt-4 font-heading text-4xl [overflow-wrap:anywhere]">{title}</h1>
    <TakeForm id={id} questions={quiz.data.questions} />
  </section>
}

function TakeForm({ id, questions }) {
  const [answers, setAnswers] = useState(() => questions.map(() => null))
  const [confirming, setConfirming] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  // State updates are async, so a ref blocks a second submit fired before the button disables.
  const sent = useRef(false)

  const unanswered = answers.filter((answer) => answer === null).length

  function choose(questionIndex, choiceIndex) {
    setAnswers((current) => current.map((answer, q) => q === questionIndex ? choiceIndex : answer))
    setConfirming(false)
  }

  async function send() {
    if (sent.current) return
    sent.current = true
    setSubmitting(true)
    setError('')
    try {
      const body = { answers: answers.flatMap((choiceIndex, questionIndex) => choiceIndex === null ? [] : [{ questionIndex, choiceIndex }]) }
      const attempt = await apiRequest(`/api/quizzes/${id}/attempts`, { method: 'POST', body })
      window.location.hash = `#/quizzes/${id}/results/${attempt.id}`
    } catch (requestError) {
      setError(errorMessage(requestError))
      setConfirming(false)
      setSubmitting(false)
      sent.current = false
    }
  }

  function submit(event) {
    event.preventDefault()
    if (unanswered > 0 && !confirming) setConfirming(true)
    else send()
  }

  return <form noValidate onSubmit={submit} className="mt-8 grid gap-6">
    {questions.map((question, q) => <Card key={q}>
      <CardContent>
        <fieldset className="grid gap-3">
          <legend className="mb-2 text-base font-medium [overflow-wrap:anywhere]"><span className="text-muted-foreground">{q + 1}.</span> {question.text}</legend>
          {question.choices.map((choice, c) => <label key={c} className="flex cursor-pointer items-center gap-3 rounded-lg border bg-card px-4 py-3 hover:border-ring has-checked:border-primary has-checked:bg-secondary">
            <input type="radio" name={`answer-${q}`} className="size-4 shrink-0 accent-[var(--forest)]" checked={answers[q] === c} onChange={() => choose(q, c)} disabled={submitting} />
            <span className="[overflow-wrap:anywhere]"><span className="font-medium">{LETTERS[c]}.</span> {choice}</span>
          </label>)}
        </fieldset>
      </CardContent>
    </Card>)}

    {confirming && <Alert role="alertdialog" aria-labelledby="confirm-submit">
      <CircleHelp aria-hidden="true" />
      <AlertDescription className="grid gap-3">
        <p id="confirm-submit" className="text-foreground">
          {unanswered === 1 ? '1 question is unanswered.' : `${unanswered} questions are unanswered.`} Unanswered questions count as wrong. Submit anyway?
        </p>
        <div className="flex gap-2">
          <Button type="submit" disabled={submitting} autoFocus>{submitting ? 'Submitting…' : 'Yes, submit'}</Button>
          <Button type="button" variant="outline" onClick={() => setConfirming(false)} disabled={submitting}>Keep answering</Button>
        </div>
      </AlertDescription>
    </Alert>}

    {error && <Alert variant="destructive"><CircleAlert aria-hidden="true" /><AlertDescription>{error}</AlertDescription></Alert>}

    {!confirming && <div>
      <Button type="submit" size="lg" disabled={submitting}>{submitting ? 'Submitting…' : 'Submit answers'}</Button>
    </div>}
  </form>
}
