import { useEffect, useState } from 'react'
import { ArrowLeft, CircleAlert, ClipboardList, Pencil, Play, Trash2 } from 'lucide-react'
import { apiRequest } from '@/api'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from '../reviewers/format'

// Overview of one quiz: linked reviewers, take, edit and delete. Answers stay in the editor only.
export default function QuizDetail({ id }) {
  const [quiz, retry] = useApiData(`/api/quizzes/${id}`)
  const [confirming, setConfirming] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const title = quiz.status === 'ready' ? quiz.data.title : 'Quiz'
  useEffect(() => { document.title = `${title} · Memoria` }, [title])

  // Deletes after confirmation. A blocked delete (quiz has attempts) shows the backend's message.
  async function deleteQuiz() {
    setDeleting(true)
    setDeleteError('')
    try {
      await apiRequest(`/api/quizzes/${id}`, { method: 'DELETE' })
      window.location.hash = '#/quizzes'
    } catch (error) {
      setDeleteError(errorMessage(error))
      setConfirming(false)
      setDeleting(false)
    }
  }

  const backLink = <a href="#/quizzes" className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />All quizzes</a>

  if (quiz.status === 'loading') return <section className="mx-auto max-w-3xl">{backLink}<p className="mt-6 text-muted-foreground" aria-live="polite">Loading quiz…</p></section>

  if (quiz.status === 'error') return <section className="mx-auto max-w-3xl">{backLink}<LoadError state={quiz} retry={retry} /></section>

  const { questions, reviewers, createdAt, hasAttempts } = quiz.data
  return <article className="mx-auto max-w-3xl">
    {backLink}
    <header className="mt-4 flex flex-wrap items-start justify-between gap-4">
      <div className="min-w-0">
        <h1 className="font-heading text-4xl [overflow-wrap:anywhere]">{title}</h1>
        <p className="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
          <Badge variant="secondary">{questions.length === 1 ? '1 question' : `${questions.length} questions`}</Badge>Created {formatDate(createdAt)}
        </p>
      </div>
      <div className="flex flex-wrap gap-2">
        <Button asChild size="lg"><a href={`#/quizzes/${id}/take`} className="no-underline"><Play aria-hidden="true" />Take quiz</a></Button>
        <Button asChild variant="outline" size="lg"><a href={`#/quizzes/${id}/edit`} className="no-underline"><Pencil aria-hidden="true" />Edit</a></Button>
        <Button variant="destructive" size="lg" onClick={() => { setConfirming(true); setDeleteError('') }} disabled={confirming}><Trash2 aria-hidden="true" />Delete</Button>
      </div>
    </header>

    {confirming && <Alert className="mt-6" role="alertdialog" aria-labelledby="confirm-delete">
      <Trash2 aria-hidden="true" />
      <AlertDescription className="grid gap-3">
        <p id="confirm-delete" className="text-foreground">Delete “{title}”? This can’t be undone. Your reviewers are not deleted.</p>
        <div className="flex gap-2">
          <Button variant="destructive" onClick={deleteQuiz} disabled={deleting} autoFocus>{deleting ? 'Deleting…' : 'Yes, delete'}</Button>
          <Button variant="outline" onClick={() => setConfirming(false)} disabled={deleting}>Cancel</Button>
        </div>
      </AlertDescription>
    </Alert>}

    {deleteError && <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription>{deleteError}</AlertDescription>
    </Alert>}

    {hasAttempts && <p className="mt-6 rounded-lg border bg-card px-4 py-3 text-sm">You’ve taken this quiz, so its questions are locked. You can still edit the title and reviewers.</p>}

    <Card className="mt-8" aria-labelledby="linked-reviewers-heading">
      <CardHeader>
        <CardTitle id="linked-reviewers-heading" className="text-xl font-normal">Linked reviewers ({reviewers.length})</CardTitle>
      </CardHeader>
      <CardContent>
        <ul className="grid list-none gap-2 p-0">
          {reviewers.map((reviewer) => <li key={reviewer.id}>
            <a href={`#/reviewers/${reviewer.id}`} className="flex items-center gap-2 rounded-lg border px-4 py-3 no-underline hover:bg-muted">
              <ClipboardList className="size-4 shrink-0" aria-hidden="true" /><span className="[overflow-wrap:anywhere]">{reviewer.title}</span>
            </a>
          </li>)}
        </ul>
      </CardContent>
    </Card>
  </article>
}

// Error block shared by the quiz screens. A 404 means missing or someone else's quiz; nothing of it is shown.
export function LoadError({ state, retry, what = 'quiz' }) {
  const notFound = state.error?.status === 404
  return <>
    <h1 className="mt-4 font-heading text-4xl">{notFound ? `${what[0].toUpperCase()}${what.slice(1)} not found` : 'Something went wrong'}</h1>
    <Alert variant="destructive" className="mt-6">
      <CircleAlert aria-hidden="true" />
      <AlertDescription className="flex flex-wrap items-center gap-3">
        {notFound ? `This ${what} doesn’t exist or isn’t yours.` : errorMessage(state.error)}
        {!notFound && <Button variant="outline" size="sm" onClick={retry}>Try again</Button>}
      </AlertDescription>
    </Alert>
  </>
}
