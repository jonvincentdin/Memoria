import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert, Upload } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { errorMessage } from '@/lib/useApiData'

const SUPPORTED_EXTENSIONS = ['md', 'txt', 'pdf', 'docx', 'pptx']
const MAX_FILE_BYTES = 5 * 1024 * 1024
const inputClass = 'block w-full rounded-md border border-input bg-white px-3.5 py-3 text-[15px] text-foreground hover:border-[#6f8575] aria-invalid:border-destructive'

function stemFromFilename(name) {
  const dot = name.lastIndexOf('.')
  return (dot > 0 ? name.slice(0, dot) : name).slice(0, 150)
}

function extensionOf(name) {
  const dot = name.lastIndexOf('.')
  return dot > 0 ? name.slice(dot + 1).toLowerCase() : ''
}

// Uploads a supported study file (.md, .txt, .pdf, .docx, .pptx) and saves it as an owned note.
export default function NoteImport() {
  const [file, setFile] = useState(null)
  const [title, setTitle] = useState('')
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [importing, setImporting] = useState(false)

  useEffect(() => { document.title = 'Import note · Memoria' }, [])

  function handleFileChange(event) {
    const selected = event.target.files?.[0] ?? null
    setFile(selected)
    setStatus('')
    setErrors((current) => ({ ...current, file: undefined }))
    if (selected && !title.trim()) {
      setTitle(stemFromFilename(selected.name))
    }
  }

  async function submit(event) {
    event.preventDefault()
    const next = {}
    if (!file) {
      next.file = 'Choose a file to import.'
    } else if (!SUPPORTED_EXTENSIONS.includes(extensionOf(file.name))) {
      next.file = 'Unsupported file type. Supported formats are .md, .txt, .pdf, .docx, and .pptx.'
    } else if (file.size === 0) {
      next.file = 'The selected file is empty.'
    } else if (file.size > MAX_FILE_BYTES) {
      next.file = 'File is too large. Maximum allowed size is 5 MB.'
    }
    if (title.trim().length > 150) {
      next.title = 'Title must be 150 characters or fewer.'
    }

    setErrors(next)
    setStatus('')
    const firstInvalid = Object.keys(next)[0]
    if (firstInvalid) {
      document.getElementById(`import-${firstInvalid}`)?.focus()
      return
    }

    const formData = new FormData()
    formData.append('file', file)
    if (title.trim()) formData.append('title', title.trim())

    setImporting(true)
    try {
      const saved = await apiRequest('/api/notes/import', { method: 'POST', body: formData })
      window.location.hash = `#/notes/${encodeURIComponent(saved.id)}`
    } catch (error) {
      setStatus(errorMessage(error))
      setImporting(false)
    }
  }

  return <section className="mx-auto max-w-3xl">
    <a href="#/notes" className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />All notes</a>
    <h1 className="mt-4 font-heading text-4xl">Import note</h1>
    <p className="mt-2 text-muted-foreground">Upload a study file and Memoria will extract its text into a personal note.</p>

    <form noValidate onSubmit={submit} className="mt-8 grid gap-6">
      <div className="grid gap-2">
        <label htmlFor="import-file" className="text-sm font-medium">File</label>
        <input
          id="import-file"
          type="file"
          accept=".md,.txt,.pdf,.docx,.pptx"
          className={`${inputClass} file:mr-4 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-secondary-foreground`}
          aria-invalid={Boolean(errors.file)}
          aria-describedby={errors.file ? 'import-file-error' : 'import-file-hint'}
          onChange={handleFileChange}
        />
        {errors.file
          ? <p id="import-file-error" className="text-sm text-destructive">{errors.file}</p>
          : <p id="import-file-hint" className="text-sm text-muted-foreground">Supported formats: .md, .txt, .pdf, .docx, .pptx (up to 5 MB).</p>}
      </div>

      <div className="grid gap-2">
        <label htmlFor="import-title" className="text-sm font-medium">Title <span className="font-normal text-muted-foreground">(optional)</span></label>
        <input
          id="import-title"
          className={inputClass}
          value={title}
          maxLength={150}
          placeholder="Defaults to the filename"
          aria-invalid={Boolean(errors.title)}
          aria-describedby={errors.title ? 'import-title-error' : 'import-title-hint'}
          onChange={(event) => { setTitle(event.target.value); setErrors((current) => ({ ...current, title: undefined })) }}
        />
        {errors.title
          ? <p id="import-title-error" className="text-sm text-destructive">{errors.title}</p>
          : <p id="import-title-hint" className="text-sm text-muted-foreground">{title.length}/150 characters</p>}
      </div>

      {status && <Alert variant="destructive"><CircleAlert aria-hidden="true" /><AlertDescription>{status}</AlertDescription></Alert>}

      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={importing}>
          <Upload aria-hidden="true" />
          {importing ? 'Importing…' : 'Import note'}
        </Button>
        <Button asChild variant="outline" size="lg"><a href="#/notes" className="no-underline">Cancel</a></Button>
      </div>
    </form>
  </section>
}
