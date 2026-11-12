import ReviewerDetail from './ReviewerDetail'
import ReviewerForm from './ReviewerForm'
import ReviewerList from './ReviewerList'

// Picks the reviewer screen from the URL: #/reviewers, #/reviewers/new, #/reviewers/{id}, #/reviewers/{id}/edit.
export default function Reviewers({ path }) {
  const [id, action] = path
  if (!id) return <ReviewerList />
  if (id === 'new') return <ReviewerForm key="new" />
  if (action === 'edit') return <ReviewerForm key={`edit-${id}`} id={id} />
  return <ReviewerDetail key={id} id={id} />
}
