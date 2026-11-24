import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert, Lock, Plus, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'

// Same limits as QuizDto on the backend.
const MAX_QUESTIONS = 20
const MAX_TITLE = 150
const MAX_TEXT = 1000
const MAX_CHOICE = 300
const LETTERS = ['A', 'B', 'C', 'D']
const inputClass = 'block w-full rounded-md border border-input bg-white px-3.5 py-3 text-[15px] text-foreground hover:border-[#6f8575] aria-invalid:border-destructive'
const blankQuestion = () => ({ text: '', choices: ['', '', '', ''], correctIndex: null })

// Create form (no id) or edit form (with id) for a quiz linked to one or more reviewers.
export default function QuizForm({ id }) {
  const editing = Boolean(id)
  const [reviewers, retryReviewers] = useApiData('/api/reviewers')
  const [existing, retryExisting] = useApiData(editing ? `/api/quizzes/${id}` : null)

  useEffect(() => { document.title = `${editing ? 'Edit quiz' : 'New quiz'} · Memoria` }, [editing])

  const failed = reviewers.status === 'error' ? reviewers : editing && existing.status === 'error' ? existing : null
  const ready = reviewers.status === 'ready' && (!editing || existing.status === 'ready')

  return <section className="mx-auto max-w-3xl">
    <a href={editing ? `#/quizzes/${id}` : '#/quizzes'} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back</a>
    <h1 className="mt-4 font-heading text-4xl">{editing ? 'Edit quiz' : 'New quiz'}</h1>

    {failed && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {failed.error?.status === 404 ? 'This quiz doesn’t exist or isn’t yours.' : errorMessage(failed.error)}
        {failed.error?.status !== 404 && <Button variant="outline" size="sm" onClick={failed === reviewers ? retryReviewers : retryExisting}>Try again</Button>}
      </AlertDescription>
    </Alert>}

    {!failed && !ready && <p className="mt-6 text-muted-foreground" aria-live="polite">Loading…</p>}

    {ready && <QuizFields id={id} reviewers={reviewers.data} initial={editing ? existing.data : null} />}
  </section>
}

// Checks one question against the backend rules; returns { field: message }.
function questionErrors(question, index) {
  const errors = {}
  if (!question.text.trim()) errors[`q${index}-text`] = 'Enter the question.'
  question.choices.forEach((choice, c) => { if (!choice.trim()) errors[`q${index}-choice${c}`] = `Enter choice ${LETTERS[c]}.` })
  if (question.correctIndex === null) errors[`q${index}-correct`] = 'Mark the correct choice.'
  return errors
}

// The form itself, shown once the student's reviewers (and the quiz being edited) have loaded.
function QuizFields({ id, reviewers, initial }) {
  const locked = Boolean(initial?.hasAttempts)
  const [title, setTitle] = useState(initial?.title ?? '')
  const [reviewerIds, setReviewerIds] = useState(() => initial?.reviewers.map((reviewer) => reviewer.id) ?? [])
  const [questions, setQuestions] = useState(() => initial?.questions.map((q) => ({ ...q, choices: [...q.choices] })) ?? [blankQuestion()])
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [saving, setSaving] = useState(false)

  const clear = (key) => setErrors((current) => ({ ...current, [key]: undefined }))

  function toggleReviewer(reviewerId) {
    setReviewerIds((current) => current.includes(reviewerId) ? current.filter((value) => value !== reviewerId) : [...current, reviewerId])
    clear('reviewerIds')
  }

  function updateQuestion(index, change, errorKey) {
    setQuestions((current) => current.map((question, q) => q === index ? { ...question, ...change(question) } : question))
    clear(errorKey)
  }

  function addQuestion() {
    setQuestions((current) => [...current, blankQuestion()])
    // Focus the new question's text once it renders.
    requestAnimationFrame(() => document.getElementById(`q${questions.length}-text`)?.focus())
  }

  function removeQuestion(index) {
    setQuestions((current) => current.filter((_, q) => q !== index))
    setErrors({}) // indexes shift, so old per-question errors no longer line up
  }

  // Checks the fields, then sends POST (create) or PUT (edit) through the shared API helper.
  async function submit(event) {
    event.preventDefault()
    const next = {}
    if (!title.trim()) next.title = 'Enter a title.'
    if (title.trim().length > MAX_TITLE) next.title = `Keep the title to ${MAX_TITLE} characters or fewer.`
    if (reviewerIds.length === 0) next.reviewerIds = 'Select at least one reviewer.'
    if (!locked) questions.forEach((question, index) => Object.assign(next, questionErrors(question, index)))
    setErrors(next)
    setStatus('')
    const firstInvalid = Object.keys(next)[0]
    if (firstInvalid) {
      document.getElementById(firstInvalid === 'reviewerIds' ? 'reviewers-legend' : firstInvalid === 'title' ? 'quiz-title' : firstInvalid.replace('-correct', '-legend'))?.focus()
      return
    }

    setSaving(true)
    try {
      // Locked quizzes resend the saved questions untouched; the backend rejects any change to them.
      const body = {
        title: title.trim(),
        mode: 'PRACTICE',
        reviewerIds,
        questions: locked ? initial.questions : questions.map((q) => ({ text: q.text.trim(), choices: q.choices.map((c) => c.trim()), correctIndex: q.correctIndex })),
      }
      const saved = await apiRequest(id ? `/api/quizzes/${id}` : '/api/quizzes', { method: id ? 'PUT' : 'POST', body })
      window.location.hash = `#/quizzes/${saved.id}`
    } catch (error) {
      setStatus(errorMessage(error))
      setSaving(false)
    }
  }

  return <form noValidate onSubmit={submit} className="grid gap-6">
    <div className="grid gap-2">
      <label htmlFor="quiz-title" className="text-sm font-medium">Title</label>
      <input id="quiz-title" className={inputClass} value={title} maxLength={MAX_TITLE} aria-invalid={Boolean(errors.title)} aria-describedby={errors.title ? 'title-error' : undefined}
        onChange={(event) => { setTitle(event.target.value); clear('title') }} />
      {errors.title && <p id="title-error" className="text-sm text-destructive">{errors.title}</p>}
    </div>

    <p className="flex items-center gap-2 text-sm"><span className="font-medium">Mode</span><Badge variant="secondary">Practice</Badge></p>

    <fieldset className="grid gap-3" aria-describedby={errors.reviewerIds ? 'reviewers-error' : undefined}>
      <legend id="reviewers-legend" tabIndex="-1" className="mb-1 text-sm font-medium">Linked reviewers <span className="font-normal text-muted-foreground">(select at least one)</span></legend>
      {reviewers.length === 0
        ? <p className="rounded-lg border bg-card px-4 py-3 text-sm">You don’t have any reviewers yet. <a href="#/reviewers/new">Create a reviewer</a> first, then come back to make a quiz.</p>
        : <ul className="grid max-h-72 list-none gap-2 overflow-y-auto p-0">
          {reviewers.map((reviewer) => <li key={reviewer.id}>
            <label className="flex cursor-pointer items-center gap-3 rounded-lg border bg-card px-4 py-3 hover:border-ring has-checked:border-primary has-checked:bg-secondary">
              <input type="checkbox" className="size-4 accent-[var(--forest)]" checked={reviewerIds.includes(reviewer.id)} onChange={() => toggleReviewer(reviewer.id)} />
              <span className="[overflow-wrap:anywhere]">{reviewer.title}</span>
            </label>
          </li>)}
        </ul>}
      {errors.reviewerIds && <p id="reviewers-error" className="text-sm text-destructive">{errors.reviewerIds}</p>}
    </fieldset>

    <div className="grid gap-4">
      <h2 className="text-lg font-medium">Questions <span className="text-sm font-normal text-muted-foreground">({questions.length} of {MAX_QUESTIONS})</span></h2>

      {locked
        ? <Alert><Lock aria-hidden="true" /><AlertDescription>This quiz already has attempts, so its questions can’t be changed. You can still edit the title and reviewers.</AlertDescription></Alert>
        : <>
          {questions.map((question, q) => <Card key={q}>
            <CardContent className="grid gap-4">
              <div className="flex items-center justify-between gap-3">
                <label htmlFor={`q${q}-text`} className="text-sm font-medium">Question {q + 1}</label>
                {questions.length > 1 && <Button type="button" variant="ghost" size="sm" onClick={() => removeQuestion(q)}><Trash2 aria-hidden="true" />Remove<span className="sr-only"> question {q + 1}</span></Button>}
              </div>
              <textarea id={`q${q}-text`} className={`${inputClass} min-h-20`} value={question.text} maxLength={MAX_TEXT} aria-invalid={Boolean(errors[`q${q}-text`])} aria-describedby={errors[`q${q}-text`] ? `q${q}-text-error` : undefined}
                onChange={(event) => updateQuestion(q, () => ({ text: event.target.value }), `q${q}-text`)} />
              {errors[`q${q}-text`] && <p id={`q${q}-text-error`} className="text-sm text-destructive">{errors[`q${q}-text`]}</p>}

              <fieldset className="grid gap-2" aria-describedby={errors[`q${q}-correct`] ? `q${q}-correct-error` : undefined}>
                <legend id={`q${q}-legend`} tabIndex="-1" className="mb-1 text-sm font-medium">Choices <span className="font-normal text-muted-foreground">(select the correct one)</span></legend>
                {question.choices.map((choice, c) => <div key={c} className="grid gap-1">
                  <div className="flex items-center gap-3">
                    <input type="radio" name={`q${q}-correct`} className="size-4 shrink-0 accent-[var(--forest)]" checked={question.correctIndex === c} aria-label={`Choice ${LETTERS[c]} is correct`}
                      onChange={() => updateQuestion(q, () => ({ correctIndex: c }), `q${q}-correct`)} />
                    <input id={`q${q}-choice${c}`} className={inputClass} value={choice} maxLength={MAX_CHOICE} placeholder={`Choice ${LETTERS[c]}`} aria-label={`Question ${q + 1}, choice ${LETTERS[c]}`} aria-invalid={Boolean(errors[`q${q}-choice${c}`])}
                      onChange={(event) => updateQuestion(q, (current) => ({ choices: current.choices.map((value, i) => i === c ? event.target.value : value) }), `q${q}-choice${c}`)} />
                  </div>
                  {errors[`q${q}-choice${c}`] && <p className="pl-7 text-sm text-destructive">{errors[`q${q}-choice${c}`]}</p>}
                </div>)}
                {errors[`q${q}-correct`] && <p id={`q${q}-correct-error`} className="text-sm text-destructive">{errors[`q${q}-correct`]}</p>}
              </fieldset>
            </CardContent>
          </Card>)}

          {questions.length < MAX_QUESTIONS && <Button type="button" variant="outline" onClick={addQuestion} className="justify-self-start"><Plus aria-hidden="true" />Add question</Button>}
        </>}
    </div>

    {status && <Alert variant="destructive"><CircleAlert aria-hidden="true" /><AlertDescription>{status}</AlertDescription></Alert>}

    <div className="flex flex-wrap gap-3">
      <Button type="submit" size="lg" disabled={saving}>{saving ? 'Saving…' : id ? 'Save changes' : 'Create quiz'}</Button>
      <Button asChild variant="outline" size="lg"><a href={id ? `#/quizzes/${id}` : '#/quizzes'} className="no-underline">Cancel</a></Button>
    </div>
  </form>
}
