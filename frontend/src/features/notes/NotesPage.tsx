import { useState, type FormEvent } from 'react'
import { Archive, ArchiveRestore, ArrowRight, Pencil, Plus, Upload } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { dateOnly } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Input } from '../../shared/ui/input'
import { Back, Empty, Failure, Field, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { DeleteAction } from '../../shared/ui/delete-action'
import type { Note } from './types'
import { ImportNote } from './ImportNote'

export function NotesPage({ path }: { path: string[] }) {
  const [id, action] = path
  if (id === 'import') return <ImportNote />
  if (id === 'new') return <NoteForm />
  if (id && action === 'edit') return <NoteForm id={id} />
  if (id) return <NoteDetail id={id} />
  return <NoteList />
}

function NoteList() {
  const [archived, setArchived] = useState(false)
  const resource = useResource<Note[]>(`/api/notes?archived=${archived}`)
  return <><PageTitle title="Notes" description="Keep the ideas, explanations, and details you want to remember." actions={<><Button asChild variant="outline"><a href="#/notes/import"><Upload aria-hidden="true" />Import file</a></Button><Button asChild><a href="#/notes/new"><Plus aria-hidden="true" />New note</a></Button></>} /><div className="tabs" aria-label="Note visibility"><Button variant="outline" aria-pressed={!archived} onClick={() => setArchived(false)}>Active notes</Button><Button variant="outline" aria-pressed={archived} onClick={() => setArchived(true)}>Archived</Button></div>
    {resource.loading ? <Loading label="Loading notes…" /> : resource.error ? <Failure error={resource.error} retry={resource.reload} /> : !resource.data?.length ? <Empty title={archived ? 'Your archive is empty' : 'A place for your next idea'} action={!archived && <Button asChild><a href="#/notes/new">Write your first note</a></Button>}>{archived ? 'Archived notes stay here until you restore or delete them.' : 'Write something new, then build on it as you learn.'}</Empty> : <ul className="resource-list">{resource.data.map(note => <li key={note.id}><a className="resource-row" href={`#/notes/${encodeURIComponent(note.id)}`}><div><h2>{note.title}</h2><p>{note.content.slice(0, 150)}{note.content.length > 150 ? '…' : ''}</p><div className="meta"><span>{dateOnly(note.createdAt)}</span><span>{note.sourceFilename ? 'Imported file' : 'Written note'}</span>{note.archived && <span className="pill">Archived</span>}</div></div><ArrowRight size={18} aria-hidden="true" /></a></li>)}</ul>}
  </>
}

function NoteDetail({ id }: { id: string }) {
  const url = `/api/notes/${encodeURIComponent(id)}`
  const resource = useResource<Note>(url)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  async function archive() { if (pending || !resource.data) return; setPending(true); setError(null); try { await apiRequest(`${url}/${resource.data.archived ? 'restore' : 'archive'}`, { method: 'POST' }); resource.reload() } catch (failure) { setError(failure) } finally { setPending(false) } }
  if (resource.loading) return <Loading label="Opening note…" />
  if (resource.error || !resource.data) return <><Back href="#/notes">All notes</Back><Failure error={resource.error} retry={resource.reload} /></>
  const note = resource.data
  return <><Back href="#/notes">All notes</Back><PageTitle title={note.title} description={`Created ${dateOnly(note.createdAt)}`} actions={<><Button asChild variant="outline"><a href={`#/notes/${encodeURIComponent(id)}/edit`}><Pencil aria-hidden="true" />Edit</a></Button><Button variant="outline" onClick={archive} disabled={pending}>{note.archived ? <ArchiveRestore aria-hidden="true" /> : <Archive aria-hidden="true" />}{pending ? 'Saving…' : note.archived ? 'Restore' : 'Archive'}</Button><DeleteAction name={note.title} remove={() => apiRequest(url, { method: 'DELETE' })} onDeleted={() => { window.location.hash = '#/notes' }} /></>} />
    <div className="meta">{note.archived && <span className="pill">Archived{note.archivedAt ? ` ${dateOnly(note.archivedAt)}` : ''}</span>}{note.sourceFilename && <span>Imported from {note.sourceFilename}</span>}</div>{!!error && <Notice error={error} />}<article className="reading section-gap">{note.content}</article>
  </>
}

function NoteForm({ id }: { id?: string }) {
  const resource = useResource<Note>(id ? `/api/notes/${encodeURIComponent(id)}` : null)
  if (id && resource.loading) return <Loading label="Loading note…" />
  if (id && resource.error) return <Failure error={resource.error} retry={resource.reload} />
  return <><Back href={id ? `#/notes/${encodeURIComponent(id)}` : '#/notes'}>Back to notes</Back><PageTitle title={id ? 'Edit note' : 'New note'} description="Start with a title. Give your ideas room to grow." /><NoteFields initial={resource.data} /></>
}

function NoteFields({ initial }: { initial: Note | null }) {
  const [title, setTitle] = useState(initial?.title || '')
  const [content, setContent] = useState(initial?.content || '')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  async function save(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (pending) return; if (!title.trim() || !content.trim()) { setError(new Error('Add a title and some content before saving.')); return } setPending(true); setError(null); try { const note = await apiRequest<Note>(initial ? `/api/notes/${encodeURIComponent(initial.id)}` : '/api/notes', { method: initial ? 'PUT' : 'POST', body: { title: title.trim(), content } }); window.location.hash = `#/notes/${note.id}` } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <form className="form-stack" onSubmit={save}><Field id="note-title" label="Title"><Input id="note-title" required maxLength={150} value={title} onChange={event => setTitle(event.target.value)} /></Field><Field id="note-content" label="Content" hint={`${content.length.toLocaleString()} / 20,000 characters`}><textarea className="textarea" id="note-content" required maxLength={20000} value={content} onChange={event => setContent(event.target.value)} /></Field>{!!error && <Notice error={error} />}<div className="actions"><Button type="submit" disabled={pending}>{pending ? 'Saving…' : 'Save note'}</Button><Button asChild variant="outline"><a href={initial ? `#/notes/${initial.id}` : '#/notes'}>Cancel</a></Button></div></form>
}
