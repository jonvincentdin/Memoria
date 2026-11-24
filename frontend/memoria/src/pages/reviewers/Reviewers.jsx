import ReviewerDetail from './ReviewerDetail'
import ReviewerForm from './ReviewerForm'
import ReviewerList from './ReviewerList'
import FlashcardForm from './FlashcardForm'
import FlashcardProgress from './FlashcardProgress'
import FlashcardStudy from './FlashcardStudy'

// Picks the reviewer or flashcard screen from the URL under #/reviewers/...
export default function Reviewers({ path }) {
  const [id, action, subAction] = path
  if (!id) return <ReviewerList />
  if (id === 'new') return <ReviewerForm key="new" />
  if (id === 'study' && action === 'due') return <FlashcardStudy key="study-all-due" dueOnly />
  if (id === 'progress') return <FlashcardProgress key="progress-all" />
  if (action === 'flashcards') {
    const [operation, flashcardId] = path.slice(2)
    if (operation === 'new') return <FlashcardForm key={`new-${id}`} reviewerId={id} />
    if (operation === 'edit' && flashcardId) return <FlashcardForm key={`edit-${flashcardId}`} reviewerId={id} id={flashcardId} />
  }
  if (action === 'study' && subAction === 'due') return <FlashcardStudy key={`study-due-${id}`} reviewerId={id} dueOnly />
  if (action === 'study') return <FlashcardStudy key={`study-${id}`} reviewerId={id} />
  if (action === 'progress') return <FlashcardProgress key={`progress-${id}`} reviewerId={id} />
  if (action === 'edit') return <ReviewerForm key={`edit-${id}`} id={id} />
  return <ReviewerDetail key={id} id={id} />
}
