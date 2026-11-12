import { ClipboardList } from 'lucide-react'
import { errorMessage, useApiData } from '@/lib/useApiData'

// Lists the reviewers made from one note. Place it on the note page: <NoteReviewers noteId={note.id} />
export default function NoteReviewers({ noteId }) {
  const [reviewers] = useApiData(`/api/reviewers?noteId=${encodeURIComponent(noteId)}`)

  return <section aria-labelledby="note-reviewers-heading" className="mt-8">
    <h2 id="note-reviewers-heading" className="font-heading text-2xl">Reviewers from this note</h2>
    {reviewers.status === 'loading' && <p className="mt-3 text-muted-foreground">Loading…</p>}
    {reviewers.status === 'error' && <p className="mt-3 text-destructive">{errorMessage(reviewers.error)}</p>}
    {reviewers.status === 'ready' && (reviewers.data.length === 0
      ? <p className="mt-3 text-muted-foreground">No reviewers use this note yet. <a href="#/reviewers/new">Create one</a>.</p>
      : <ul className="mt-3 grid list-none gap-2 p-0">
        {reviewers.data.map((reviewer) => <li key={reviewer.id}>
          <a href={`#/reviewers/${reviewer.id}`} className="flex items-center gap-2 rounded-lg border px-4 py-3 no-underline hover:bg-muted">
            <ClipboardList className="size-4 shrink-0" aria-hidden="true" />
            <span className="[overflow-wrap:anywhere]">{reviewer.title}</span>
            <span className="ml-auto text-xs text-muted-foreground">{reviewer.style}</span>
          </a>
        </li>)}
      </ul>)}
  </section>
}
