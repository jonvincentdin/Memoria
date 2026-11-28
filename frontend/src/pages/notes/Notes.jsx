import NoteDetail from './NoteDetail'
import NoteForm from './NoteForm'
import NoteImport from './NoteImport'
import NoteList from './NoteList'

// Chooses the notes screen from the current hash route.
export default function Notes({ path }) {
  const [id, action] = path
  if (!id) return <NoteList />
  if (id === 'new') return <NoteForm key="new" />
  if (id === 'import') return <NoteImport key="import" />
  if (action === 'edit') return <NoteForm key={`edit-${id}`} id={id} />
  return <NoteDetail key={id} id={id} />
}
