import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { errorMessage, useApiData } from '@/lib/useApiData'

const inputClass = 'block w-full rounded-md border border-input bg-white px-3.5 py-3 text-[15px] text-foreground hover:border-[#6f8575] aria-invalid:border-destructive'

export default function FlashcardForm({ reviewerId, id }) {
  const editing = Boolean(id)
  const [existing, retryExisting] = useApiData(editing ? `/api/flashcards/${encodeURIComponent(id)}` : null)

  useEffect(() => { document.title = `${editing ? 'Edit flashcard' : 'New flashcard'} · Memoria` }, [editing])

  if (editing && existing.status === 'loading') return <FlashcardFormState reviewerId={reviewerId} title="Edit flashcard" message="Loading flashcard…" />
  if (editing && existing.status === 'error') return <FlashcardFormState reviewerId={reviewerId} title="Edit flashcard" error={errorMessage(existing.error)} onRetry={retryExisting} />
  if (editing && existing.data.reviewerId !== reviewerId) return <FlashcardFormState reviewerId={reviewerId} title="Edit flashcard" error="This flashcard is not linked to this reviewer." />

  return <FlashcardFields key={id || 'new'} reviewerId={reviewerId} id={id} initial={editing ? existing.data : null} />
}

function FlashcardFormState({ reviewerId, title, message, error, onRetry }) {
  return <section className="mx-auto max-w-3xl">
    <a href={`#/reviewers/${encodeURIComponent(reviewerId)}`} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">{title}</h1>
    {message && <p className="mt-6 text-muted-foreground" aria-live="polite">{message}</p>}
    {error && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {error}
        {onRetry && <Button variant="outline" size="sm" onClick={onRetry}>Try again</Button>}
      </AlertDescription>
    </Alert>}
  </section>
}

function FlashcardFields({ reviewerId, id, initial }) {
  const editing = Boolean(id)
  const [front, setFront] = useState(initial?.front ?? '')
  const [back, setBack] = useState(initial?.back ?? '')
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    const next = {}
    if (!front.trim()) next.front = 'Enter the front of the flashcard.'
    if (!back.trim()) next.back = 'Enter the back of the flashcard.'
    setErrors(next)
    setStatus('')
    const firstInvalid = Object.keys(next)[0]
    if (firstInvalid) {
      document.getElementById(`flashcard-${firstInvalid}`)?.focus()
      return
    }

    setSaving(true)
    try {
      const saved = await apiRequest(
        editing ? `/api/flashcards/${encodeURIComponent(id)}` : '/api/flashcards',
        {
          method: editing ? 'PUT' : 'POST',
          body: { reviewerId, front: front.trim(), back: back.trim() },
        },
      )
      window.location.hash = `#/reviewers/${encodeURIComponent(reviewerId)}`
      return saved
    } catch (error) {
      setStatus(errorMessage(error))
      setSaving(false)
    }
  }

  const backHref = `#/reviewers/${encodeURIComponent(reviewerId)}`

  return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to reviewer</a>
    <h1 className="mt-4 font-heading text-4xl">{editing ? 'Edit flashcard' : 'New flashcard'}</h1>

    {status && <Alert variant="destructive" className="mt-6" role="alert">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{status}</AlertDescription>
    </Alert>}

    <form noValidate onSubmit={submit} className="mt-6 grid gap-6">
      <div className="grid gap-2">
        <label htmlFor="flashcard-front" className="text-sm font-medium">Front</label>
        <textarea id="flashcard-front" className={`${inputClass} min-h-32 leading-relaxed`} value={front} aria-invalid={Boolean(errors.front)} aria-describedby={errors.front ? 'flashcard-front-error' : undefined}
          onChange={(event) => { setFront(event.target.value); setErrors((current) => ({ ...current, front: undefined })) }} />
        {errors.front && <p id="flashcard-front-error" className="text-sm text-destructive">{errors.front}</p>}
      </div>

      <div className="grid gap-2">
        <label htmlFor="flashcard-back" className="text-sm font-medium">Back</label>
        <textarea id="flashcard-back" className={`${inputClass} min-h-32 leading-relaxed`} value={back} aria-invalid={Boolean(errors.back)} aria-describedby={errors.back ? 'flashcard-back-error' : undefined}
          onChange={(event) => { setBack(event.target.value); setErrors((current) => ({ ...current, back: undefined })) }} />
        {errors.back && <p id="flashcard-back-error" className="text-sm text-destructive">{errors.back}</p>}
      </div>

      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={saving}>{saving ? 'Saving…' : editing ? 'Save changes' : 'Create flashcard'}</Button>
        <Button asChild variant="outline" size="lg"><a href={backHref} className="no-underline">Cancel</a></Button>
      </div>
    </form>
  </section>
}
