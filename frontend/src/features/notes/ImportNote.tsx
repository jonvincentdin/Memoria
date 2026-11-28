import { useState, type FormEvent } from 'react'
import { Upload } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { Button } from '../../shared/ui/button'
import { Input } from '../../shared/ui/input'
import { Back, Field, Notice, PageTitle } from '../../shared/ui/common'
import type { Note } from './types'

const formats = ['md', 'txt', 'pdf', 'docx', 'pptx']

export function ImportNote() {
  const [file, setFile] = useState<File | null>(null)
  const [title, setTitle] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  async function upload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (pending) return
    setError(null)
    if (!file) { setError(new Error('Choose a file to import.')); return }
    if (!formats.includes(file.name.split('.').pop()?.toLowerCase() || '')) { setError(new Error('Choose an MD, TXT, PDF, DOCX, or PPTX file.')); return }
    if (!file.size) { setError(new Error('This file is empty. Choose a file with text to import.')); return }
    if (file.size > 5 * 1024 * 1024) { setError(new Error('This file is larger than 5 MB. Choose a smaller file.')); return }
    const body = new FormData(); body.append('file', file); if (title.trim()) body.append('title', title.trim())
    setPending(true)
    try { const note = await apiRequest<Note>('/api/notes/import', { method: 'POST', body }); window.location.hash = `#/notes/${note.id}` } catch (failure) { setError(failure) } finally { setPending(false) }
  }
  return <><Back href="#/notes">All notes</Back><PageTitle title="Import a note" description="Bring your study material into one place. We’ll extract the text into an editable note." /><form className="form-stack" onSubmit={upload}><div className="upload-surface"><Upload size={28} aria-hidden="true" /><Field id="import-file" label="Choose your study file" hint="MD, TXT, PDF, DOCX, or PPTX. Maximum 5 MB."><Input id="import-file" type="file" accept=".md,.txt,.pdf,.docx,.pptx" disabled={pending} onChange={event => { setFile(event.target.files?.[0] || null); setError(null) }} aria-describedby="import-file-hint" /></Field>{file && <p className="meta">{file.name} · {(file.size / 1024).toFixed(1)} KB</p>}</div><Field id="import-title" label="Title (optional)" hint="Leave this blank to use the filename."><Input id="import-title" maxLength={150} value={title} onChange={event => setTitle(event.target.value)} /></Field><p className="muted import-explanation">Imports extract text; they do not reproduce the original page layout. Scanned or image-only documents need text recognition before they can be imported.</p>{!!error && <Notice error={error} />}<div className="actions"><Button type="submit" disabled={pending}><Upload aria-hidden="true" />{pending ? 'Importing…' : 'Import file'}</Button><Button asChild variant="outline"><a href="#/notes">Cancel</a></Button></div></form></>
}
