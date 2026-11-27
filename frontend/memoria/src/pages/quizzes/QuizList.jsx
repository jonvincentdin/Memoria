import { useEffect } from 'react'
import { CircleAlert, ClipboardList, Plus, Target } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { errorMessage, useApiData } from '@/lib/useApiData'
import { formatDate } from '../reviewers/format'

// Lists the logged-in student's quizzes with loading, empty and error states. Never shows answers.
export default function QuizList() {
  const [quizzes, retry] = useApiData('/api/quizzes')

  useEffect(() => { document.title = 'Quizzes · Memoria' }, [])

  return <section>
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-heading text-4xl">Quizzes</h1>
        <p className="mt-2 text-muted-foreground">Practice with questions built from your reviewers.</p>
      </div>
      <Button asChild size="lg"><a href="#/quizzes/new" className="no-underline"><Plus aria-hidden="true" />New quiz</a></Button>
    </div>

    <div className="mt-8" aria-live="polite">
      {quizzes.status === 'loading' && <p className="text-muted-foreground">Loading your quizzes…</p>}

      {quizzes.status === 'error' && <Alert variant="destructive">
        <CircleAlert aria-hidden="true" />
        <AlertDescription className="flex flex-wrap items-center gap-3">
          {errorMessage(quizzes.error)}
          <Button variant="outline" size="sm" onClick={retry}>Try again</Button>
        </AlertDescription>
      </Alert>}

      {quizzes.status === 'ready' && quizzes.data.length === 0 && <Card className="items-center py-12 text-center">
        <span className="grid size-14 place-items-center rounded-2xl bg-secondary text-secondary-foreground"><Target className="size-7" aria-hidden="true" /></span>
        <CardTitle className="text-2xl font-normal">No quizzes yet</CardTitle>
        <CardDescription>Create your first quiz from one or more of your reviewers.</CardDescription>
        <Button asChild size="lg"><a href="#/quizzes/new" className="no-underline"><Plus aria-hidden="true" />New quiz</a></Button>
      </Card>}

      {quizzes.status === 'ready' && quizzes.data.length > 0 && <ul className="grid list-none gap-4 p-0 md:grid-cols-2">
        {quizzes.data.map((quiz) => <li key={quiz.id}>
          <a href={`#/quizzes/${quiz.id}`} className="group block h-full rounded-xl text-inherit no-underline">
            <Card className="h-full transition-shadow group-hover:shadow-md">
              <CardHeader>
                <div className="flex items-start justify-between gap-3">
                  <CardTitle className="text-xl font-normal [overflow-wrap:anywhere]">{quiz.title}</CardTitle>
                  <Badge variant="secondary">{quiz.questions.length === 1 ? '1 question' : `${quiz.questions.length} questions`}</Badge>
                </div>
                <CardDescription>Created {formatDate(quiz.createdAt)}</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="flex flex-wrap items-center gap-1.5 text-xs text-muted-foreground">
                  <ClipboardList className="size-3.5" aria-hidden="true" />
                  {quiz.reviewers.length === 1 ? 'From 1 reviewer:' : `From ${quiz.reviewers.length} reviewers:`}
                  {quiz.reviewers.map((reviewer) => <Badge key={reviewer.id} variant="outline">{reviewer.title}</Badge>)}
                </p>
              </CardContent>
            </Card>
          </a>
        </li>)}
      </ul>}
    </div>
  </section>
}
