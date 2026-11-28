import { useState, type FormEvent } from 'react'
import { ArrowRight, Pencil, Plus } from 'lucide-react'
import { apiRequest } from '../../shared/api'
import { useResource } from '../../shared/api/use-resource'
import { dateOnly } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Input } from '../../shared/ui/input'
import { Back, Empty, Failure, Field, Loading, Notice, PageTitle } from '../../shared/ui/common'
import { DeleteAction } from '../../shared/ui/delete-action'
import type { Reviewer } from './types'
import type { Note } from '../notes/types'
import { ReviewerFlashcards } from '../flashcards/FlashcardsPage'

export function ReviewersPage({ path }: { path: string[] }) {
  const [id, action] = path
  if (id === 'new') return <ReviewerForm />
  if (id && action === 'edit') return <ReviewerForm id={id} />
  if (id) return <ReviewerDetail id={id} />
  return <ReviewerList />
}

function ReviewerList() {
  const resource = useResource<Reviewer[]>('/api/reviewers')
  return <><PageTitle title="Reviewers" description="Make connections between your notes and shape them into a study guide." actions={<Button asChild><a href="#/reviewers/new"><Plus aria-hidden="true" />New reviewer</a></Button>} />{resource.loading ? <Loading label="Loading reviewers…" /> : resource.error ? <Failure error={resource.error} retry={resource.reload} /> : !resource.data?.length ? <Empty title="Bring your notes together" action={<Button asChild><a href="#/reviewers/new">Create a reviewer</a></Button>}>Choose your source notes, then write a summary, outline, or question-and-answer guide.</Empty> : <ul className="resource-list">{resource.data.map(reviewer => <li key={reviewer.id}><a className="resource-row" href={`#/reviewers/${encodeURIComponent(reviewer.id)}`}><div><h2>{reviewer.title}</h2><p>{reviewer.content.slice(0, 150)}{reviewer.content.length > 150 ? '…' : ''}</p><div className="meta"><span>{dateOnly(reviewer.createdAt)}</span><span>{reviewer.notes.length} source {reviewer.notes.length === 1 ? 'note' : 'notes'}</span><span className="pill">{reviewer.style.replaceAll('_', ' ')}</span></div></div><ArrowRight size={18} aria-hidden="true" /></a></li>)}</ul>}</>
}

function ReviewerDetail({ id }: { id: string }) {
  const url = `/api/reviewers/${encodeURIComponent(id)}`
  const resource = useResource<Reviewer>(url)
  if (resource.loading) return <Loading label="Opening reviewer…" />
  if (resource.error || !resource.data) return <><Back href="#/reviewers">All reviewers</Back><Failure error={resource.error} retry={resource.reload} /></>
  const reviewer = resource.data
  return <><Back href="#/reviewers">All reviewers</Back><PageTitle title={reviewer.title} description={`Created ${dateOnly(reviewer.createdAt)}`} actions={<><Button asChild variant="outline"><a href={`#/reviewers/${encodeURIComponent(id)}/edit`}><Pencil aria-hidden="true" />Edit</a></Button><DeleteAction name={reviewer.title} remove={() => apiRequest(url, { method: 'DELETE' })} onDeleted={() => { window.location.hash = '#/reviewers' }} /></>} /><p className="meta"><span className="pill">{reviewer.style.replaceAll('_', ' ')}</span></p><article className="reading section-gap">{reviewer.content}</article><section className="section-gap"><h2>Source notes</h2><ul className="linked-list">{reviewer.notes.map(note => <li key={note.id}><a href={`#/notes/${encodeURIComponent(note.id)}`}>{note.title}<ArrowRight size={16} aria-hidden="true" /></a></li>)}</ul></section><ReviewerFlashcards reviewerId={id} /></>
}

function ReviewerForm({ id }: { id?: string }) {
  const resource = useResource<Reviewer>(id ? `/api/reviewers/${encodeURIComponent(id)}` : null)
  const active = useResource<Note[]>('/api/notes')
  const archived = useResource<Note[]>('/api/notes?archived=true')
  if (active.loading || archived.loading || (id && resource.loading)) return <Loading label="Loading source notes…" />
  const error = resource.error || active.error || archived.error
  if (error) return <Failure error={error} retry={() => { resource.reload(); active.reload(); archived.reload() }} />
  const notes = [...(active.data || []), ...(archived.data || [])]
  return <><Back href={id ? `#/reviewers/${encodeURIComponent(id)}` : '#/reviewers'}>Back to reviewers</Back><PageTitle title={id ? 'Edit reviewer' : 'New reviewer'} description="Select your source notes and put the important ideas in your own words." />{!notes.length ? <Empty title="Start with a note" action={<Button asChild><a href="#/notes/new">Create a note</a></Button>}>A reviewer needs at least one source note. Create or import a note first.</Empty> : <ReviewerFields initial={resource.data} notes={notes} />}</>
}

function ReviewerFields({ initial, notes }: { initial: Reviewer | null; notes: Note[] }) {
  const [title, setTitle] = useState(initial?.title || '')
  const [style, setStyle] = useState(initial?.style || 'SUMMARY')
  const [content, setContent] = useState(initial?.content || '')
  const [selected, setSelected] = useState(initial?.notes.map(note => note.id) || [])
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const styles = ['SUMMARY', 'OUTLINE', 'QUESTIONS_AND_ANSWERS']
  if (!styles.includes(style)) styles.push(style)
  async function save(event: FormEvent<HTMLFormElement>) { event.preventDefault(); if (pending) return; if (!selected.length || !title.trim() || !content.trim()) { setError(new Error('Add a title, content, and at least one source note.')); if (!selected.length) document.getElementById('source-notes')?.focus(); return } setPending(true); setError(null); try { const reviewer = await apiRequest<Reviewer>(initial ? `/api/reviewers/${encodeURIComponent(initial.id)}` : '/api/reviewers', { method: initial ? 'PUT' : 'POST', body: { title: title.trim(), style, content, noteIds: selected } }); window.location.hash = `#/reviewers/${reviewer.id}` } catch (failure) { setError(failure) } finally { setPending(false) } }
  return <form className="form-stack" onSubmit={save}><Field id="reviewer-title" label="Title"><Input id="reviewer-title" required maxLength={200} value={title} onChange={event => setTitle(event.target.value)} /></Field><Field id="reviewer-style" label="Study style"><select id="reviewer-style" className="select" value={style} onChange={event => setStyle(event.target.value)}>{styles.map(value => <option key={value} value={value}>{value.replaceAll('_', ' ')}</option>)}</select></Field><fieldset><legend id="source-notes" tabIndex={-1} className="field-label">Source notes — choose at least one</legend><ul className="checkbox-list">{notes.map(note => <li key={note.id}><label><input type="checkbox" value={note.id} checked={selected.includes(note.id)} onChange={() => setSelected(current => current.includes(note.id) ? current.filter(id => id !== note.id) : [...current, note.id])} /><span>{note.title}{note.archived && <span className="pill">Archived</span>}</span></label></li>)}</ul></fieldset><Field id="reviewer-content" label="Study guide content"><textarea className="textarea" id="reviewer-content" required value={content} onChange={event => setContent(event.target.value)} /></Field>{!!error && <Notice error={error} />}<div className="actions"><Button type="submit" disabled={pending}>{pending ? 'Saving…' : 'Save reviewer'}</Button><Button asChild variant="outline"><a href={initial ? `#/reviewers/${initial.id}` : '#/reviewers'}>Cancel</a></Button></div></form>
}

export function RelatedReviewers({ noteId }: { noteId: string }) {
  const resource = useResource<Reviewer[]>(`/api/reviewers?noteId=${encodeURIComponent(noteId)}`)
  return <section className="section-gap"><h2>Reviewers using this note</h2>{resource.loading ? <Loading label="Loading linked reviewers…" /> : resource.error ? <Failure error={resource.error} retry={resource.reload} /> : !resource.data?.length ? <p className="muted linked-empty">No reviewers use this note yet. <a href="#/reviewers/new">Create a reviewer</a> to connect your notes.</p> : <ul className="linked-list">{resource.data.map(reviewer => <li key={reviewer.id}><a data-linked-reviewer href={`#/reviewers/${encodeURIComponent(reviewer.id)}`}>{reviewer.title}<ArrowRight size={16} aria-hidden="true" /></a></li>)}</ul>}</section>
}
