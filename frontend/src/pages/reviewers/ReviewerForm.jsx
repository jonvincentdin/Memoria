import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { errorMessage, useApiData } from '@/lib/useApiData'

const STYLE_SUGGESTIONS = ['Summary', 'Outline', 'Q&A', 'Key terms']
const inputClass = 'block w-full rounded-md border border-input bg-white px-3.5 py-3 text-[15px] text-foreground hover:border-[#6f8575] aria-invalid:border-destructive'

// Create form (no id) or edit form (with id) for a reviewer linked to one or more notes.
export default function ReviewerForm({ id }) {
  const editing = Boolean(id)
  const [notes, retryNotes] = useApiData('/api/notes')
  const [existing, retryExisting] = useApiData(editing ? `/api/reviewers/${id}` : null)

  useEffect(() => { document.title = `${editing ? 'Edit reviewer' : 'New reviewer'} · Memoria` }, [editing])

  const backHref = editing ? `#/reviewers/${id}` : '#/reviewers'
  const failed = notes.status === 'error' ? notes : editing && existing.status === 'error' ? existing : null
  const ready = notes.status === 'ready' && (!editing || existing.status === 'ready')

  return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back</a>
    <h1 className="mt-4 font-heading text-4xl">{editing ? 'Edit reviewer' : 'New reviewer'}</h1>

    {failed && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {errorMessage(failed.error)}
        <Button variant="outline" size="sm" onClick={failed === notes ? retryNotes : retryExisting}>Try again</Button>
      </AlertDescription>
    </Alert>}

    {!failed && !ready && <p className="mt-6 text-muted-foreground" aria-live="polite">Loading…</p>}

    {ready && <ReviewerFields id={id} notes={notes.data} initial={editing ? existing.data : null} />}
  </section>
}

// The form itself, shown once the student's notes (and the reviewer being edited) have loaded.
function ReviewerFields({ id, notes, initial }) {
  const [title, setTitle] = useState(initial?.title ?? '')
  const [style, setStyle] = useState(initial?.style ?? '')
  const [content, setContent] = useState(initial?.content ?? '')
  const [noteIds, setNoteIds] = useState(() => initial?.notes.map((note) => note.id) ?? [])
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [saving, setSaving] = useState(false)

  // Adds or removes a note from the selected notes.
  function toggleNote(noteId) {
    setNoteIds((current) => current.includes(noteId) ? current.filter((value) => value !== noteId) : [...current, noteId])
    setErrors((current) => ({ ...current, noteIds: undefined }))
  }

  // Checks the fields, then sends POST (create) or PUT (edit) through the shared API helper.
  async function submit(event) {
    event.preventDefault()
    const next = {}
    if (!title.trim()) next.title = 'Enter a title.'
    if (!style.trim()) next.style = 'Enter a style, such as Summary or Outline.'
    if (!content.trim()) next.content = 'Enter the reviewer content.'
    if (noteIds.length === 0) next.noteIds = 'Select at least one note.'
    setErrors(next)
    setStatus('')
    const firstInvalid = Object.keys(next)[0]
    if (firstInvalid) {
      document.getElementById(firstInvalid === 'noteIds' ? 'notes-legend' : `reviewer-${firstInvalid}`)?.focus()
      return
    }

    setSaving(true)
    try {
      const body = { title: title.trim(), style: style.trim(), content, noteIds }
      const saved = await apiRequest(id ? `/api/reviewers/${id}` : '/api/reviewers', { method: id ? 'PUT' : 'POST', body })
      window.location.hash = `#/reviewers/${saved.id}`
    } catch (error) {
      setStatus(errorMessage(error))
      setSaving(false)
    }
  }

  return <form noValidate onSubmit={submit} className="grid gap-6">
    <div className="grid gap-2">
      <label htmlFor="reviewer-title" className="text-sm font-medium">Title</label>
      <input id="reviewer-title" className={inputClass} value={title} maxLength={200} aria-invalid={Boolean(errors.title)} aria-describedby={errors.title ? 'title-error' : undefined}
        onChange={(event) => { setTitle(event.target.value); setErrors((current) => ({ ...current, title: undefined })) }} />
      {errors.title && <p id="title-error" className="text-sm text-destructive">{errors.title}</p>}
    </div>

    <div className="grid gap-2">
      <label htmlFor="reviewer-style" className="text-sm font-medium">Style</label>
      <input id="reviewer-style" className={inputClass} value={style} maxLength={50} list="reviewer-style-options" aria-invalid={Boolean(errors.style)} aria-describedby={errors.style ? 'style-error' : 'style-hint'}
        onChange={(event) => { setStyle(event.target.value); setErrors((current) => ({ ...current, style: undefined })) }} />
      <datalist id="reviewer-style-options">{STYLE_SUGGESTIONS.map((option) => <option key={option} value={option} />)}</datalist>
      {errors.style
        ? <p id="style-error" className="text-sm text-destructive">{errors.style}</p>
        : <p id="style-hint" className="text-sm text-muted-foreground">A short label, for example Summary, Outline, or Q&amp;A.</p>}
    </div>

    <div className="grid gap-2">
      <label htmlFor="reviewer-content" className="text-sm font-medium">Content</label>
      <textarea id="reviewer-content" className={`${inputClass} min-h-64 leading-relaxed`} value={content} aria-invalid={Boolean(errors.content)} aria-describedby={errors.content ? 'content-error' : 'content-hint'}
        onChange={(event) => { setContent(event.target.value); setErrors((current) => ({ ...current, content: undefined })) }} />
      {errors.content
        ? <p id="content-error" className="text-sm text-destructive">{errors.content}</p>
        : <p id="content-hint" className="text-sm text-muted-foreground">Type or paste your reviewer. Line breaks are kept.</p>}
    </div>

    <fieldset className="grid gap-3" aria-describedby={errors.noteIds ? 'notes-error' : undefined}>
      <legend id="notes-legend" tabIndex="-1" className="mb-1 text-sm font-medium">Linked notes <span className="font-normal text-muted-foreground">(select at least one)</span></legend>
      {notes.length === 0
        ? <p className="rounded-lg border bg-card px-4 py-3 text-sm">You don’t have any notes yet. <a href="#/notes">Create a note</a> first, then come back to make a reviewer.</p>
        : <ul className="grid max-h-72 list-none gap-2 overflow-y-auto p-0">
          {notes.map((note) => <li key={note.id}>
            <label className="flex cursor-pointer items-center gap-3 rounded-lg border bg-card px-4 py-3 hover:border-ring has-checked:border-primary has-checked:bg-secondary">
              <input type="checkbox" className="size-4 accent-[var(--forest)]" checked={noteIds.includes(note.id)} onChange={() => toggleNote(note.id)} />
              <span className="[overflow-wrap:anywhere]">{note.title}</span>
            </label>
          </li>)}
        </ul>}
      {errors.noteIds && <p id="notes-error" className="text-sm text-destructive">{errors.noteIds}</p>}
    </fieldset>

    {status && <Alert variant="destructive"><CircleAlert aria-hidden="true" /><AlertDescription>{status}</AlertDescription></Alert>}

    <div className="flex flex-wrap gap-3">
      <Button type="submit" size="lg" disabled={saving}>{saving ? 'Saving…' : id ? 'Save changes' : 'Create reviewer'}</Button>
      <Button asChild variant="outline" size="lg"><a href={id ? `#/reviewers/${id}` : '#/reviewers'} className="no-underline">Cancel</a></Button>
    </div>
  </form>
}
