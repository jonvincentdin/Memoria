import ReviewerDetail from './ReviewerDetail'
import ReviewerForm from './ReviewerForm'
import ReviewerList from './ReviewerList'
import FlashcardForm from './FlashcardForm'
import FlashcardStudy from './FlashcardStudy'

// Picks the reviewer screen from the URL: #/reviewers, #/reviewers/new, #/reviewers/{id}, #/reviewers/{id}/edit.
export default function Reviewers({ path }) {
  const [id, action] = path
  if (!id) return <ReviewerList />
  if (id === 'new') return <ReviewerForm key="new" />
  if (action === 'flashcards') {
    const [operation, flashcardId] = path.slice(2)
    if (operation === 'new') return <FlashcardForm key={`new-${id}`} reviewerId={id} />
    if (operation === 'edit' && flashcardId) return <FlashcardForm key={`edit-${flashcardId}`} reviewerId={id} id={flashcardId} />
  }
  if (action === 'study') return <FlashcardStudy key={`study-${id}`} reviewerId={id} />
  if (action === 'edit') return <ReviewerForm key={`edit-${id}`} id={id} />
  return <ReviewerDetail key={id} id={id} />
}
