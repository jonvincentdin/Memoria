import QuizDetail from './QuizDetail'
import QuizForm from './QuizForm'
import QuizList from './QuizList'
import QuizResult from './QuizResult'
import QuizTake from './QuizTake'

// Picks the quiz screen from the URL: #/quizzes, #/quizzes/new, #/quizzes/{id}, #/quizzes/{id}/edit,
// #/quizzes/{id}/take, #/quizzes/{id}/results/{attemptId}.
export default function Quizzes({ path }) {
  const [id, action, attemptId] = path
  if (!id) return <QuizList />
  if (id === 'new') return <QuizForm key="new" />
  if (action === 'edit') return <QuizForm key={`edit-${id}`} id={id} />
  if (action === 'take') return <QuizTake key={`take-${id}`} id={id} />
  if (action === 'results' && attemptId) return <QuizResult key={attemptId} quizId={id} attemptId={attemptId} />
  return <QuizDetail key={id} id={id} />
}
