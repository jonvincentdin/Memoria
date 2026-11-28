import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { errorMessage, useApiData } from '@/lib/useApiData'

const inputClass = 'block w-full rounded-md border border-input bg-white px-3.5 py-3 text-[15px] text-foreground hover:border-[#6f8575] aria-invalid:border-destructive'

// Loads an existing note for editing, or opens the shared fields for a new note.
export default function NoteForm({ id }) {
  const editing = Boolean(id)
  const [existing, retry] = useApiData(editing ? `/api/notes/${encodeURIComponent(id)}` : null)

  useEffect(() => { document.title = `${editing ? 'Edit note' : 'New note'} · Memoria` }, [editing])

  const backHref = editing ? `#/notes/${encodeURIComponent(id)}` : '#/notes'

  if (existing.status === 'error') {
    const notFound = existing.error?.status === 404
    return <section className="mx-auto max-w-3xl">
      <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back</a>
      <h1 className="mt-4 font-heading text-4xl">{notFound ? 'Note not found' : 'Could not load note'}</h1>
      <Alert variant="destructive" className="mt-6">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {notFound ? 'This note doesn’t exist or isn’t yours.' : errorMessage(existing.error)}
          {!notFound && <Button variant="outline" size="sm" onClick={retry}>Try again</Button>}
        </AlertDescription>
      </Alert>
    </section>
  }

  if (editing && existing.status !== 'ready') {
    return <section className="mx-auto max-w-3xl">
      <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back</a>
      <p className="mt-6 text-muted-foreground" aria-live="polite">Loading note…</p>
    </section>
  }

  return <section className="mx-auto max-w-3xl">
    <a href={backHref} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back</a>
    <h1 className="mt-4 font-heading text-4xl">{editing ? 'Edit note' : 'New note'}</h1>
    <NoteFields id={id} initial={editing ? existing.data : null} />
  </section>
}

// Validates and saves the title and content for both create and edit requests.
function NoteFields({ id, initial }) {
  const [title, setTitle] = useState(initial?.title ?? '')
  const [content, setContent] = useState(initial?.content ?? '')
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event) {
    event.preventDefault()
    const next = {}
    if (!title.trim()) next.title = 'Enter a title.'
    if (!content.trim()) next.content = 'Enter the note content.'
    setErrors(next)
    setStatus('')

    const firstInvalid = Object.keys(next)[0]
    if (firstInvalid) {
      document.getElementById(`note-${firstInvalid}`)?.focus()
      return
    }

    setSaving(true)
    try {
      const saved = await apiRequest(id ? `/api/notes/${encodeURIComponent(id)}` : '/api/notes', {
        method: id ? 'PUT' : 'POST',
        body: { title: title.trim(), content },
      })
      window.location.hash = `#/notes/${encodeURIComponent(saved.id)}`
    } catch (error) {
      setStatus(errorMessage(error))
      setSaving(false)
    }
  }

  return <form noValidate onSubmit={submit} className="grid gap-6">
    <div className="grid gap-2">
      <label htmlFor="note-title" className="text-sm font-medium">Title</label>
      <input id="note-title" className={inputClass} value={title} maxLength={150} aria-invalid={Boolean(errors.title)} aria-describedby={errors.title ? 'note-title-error' : 'note-title-hint'}
        onChange={(event) => { setTitle(event.target.value); setErrors((current) => ({ ...current, title: undefined })) }} />
      {errors.title
        ? <p id="note-title-error" className="text-sm text-destructive">{errors.title}</p>
        : <p id="note-title-hint" className="text-sm text-muted-foreground">{title.length}/150 characters</p>}
    </div>

    <div className="grid gap-2">
      <label htmlFor="note-content" className="text-sm font-medium">Content</label>
      <textarea id="note-content" className={`${inputClass} min-h-64 leading-relaxed`} value={content} maxLength={20000} aria-invalid={Boolean(errors.content)} aria-describedby={errors.content ? 'note-content-error' : 'note-content-hint'}
        onChange={(event) => { setContent(event.target.value); setErrors((current) => ({ ...current, content: undefined })) }} />
      {errors.content
        ? <p id="note-content-error" className="text-sm text-destructive">{errors.content}</p>
        : <p id="note-content-hint" className="text-sm text-muted-foreground">Up to 20,000 characters. Line breaks are kept.</p>}
    </div>

    {status && <Alert variant="destructive"><CircleAlert aria-hidden="true" /><AlertDescription>{status}</AlertDescription></Alert>}

    <div className="flex flex-wrap gap-3">
      <Button type="submit" size="lg" disabled={saving}>{saving ? 'Saving…' : id ? 'Save changes' : 'Create note'}</Button>
      <Button asChild variant="outline" size="lg"><a href={id ? `#/notes/${encodeURIComponent(id)}` : '#/notes'} className="no-underline">Cancel</a></Button>
    </div>
  </form>
}
