import { useEffect } from 'react'
import { ArrowLeft, RotateCcw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useApiData } from '@/lib/useApiData'
import { LoadError } from './QuizDetail'

// Shows one saved attempt, as returned by the backend after grading.
export default function QuizResult({ quizId, attemptId }) {
  const [attempt, retry] = useApiData(`/api/attempts/${attemptId}`)
  useEffect(() => { document.title = 'Quiz result · Memoria' }, [])

  const backLink = <a href={`#/quizzes/${quizId}`} className="inline-flex items-center gap-1.5 text-sm no-underline hover:underline"><ArrowLeft className="size-4" aria-hidden="true" />Back to quiz</a>

  if (attempt.status === 'loading') return <section className="mx-auto max-w-xl">{backLink}<p className="mt-6 text-muted-foreground" aria-live="polite">Loading result…</p></section>
  if (attempt.status === 'error') return <section className="mx-auto max-w-xl">{backLink}<LoadError state={attempt} retry={retry} what="result" /></section>

  const { quizTitle, score, totalQuestions, percentage } = attempt.data
  return <section className="mx-auto max-w-xl">
    {backLink}
    <Card className="mt-6 items-center py-10 text-center">
      <CardHeader className="w-full">
        <CardDescription className="[overflow-wrap:anywhere]">{quizTitle}</CardDescription>
        <CardTitle><h1 className="font-heading text-5xl font-normal">{score} / {totalQuestions}</h1></CardTitle>
      </CardHeader>
      <CardContent className="grid gap-6">
        <p className="text-lg text-muted-foreground">{Math.round(percentage)}% correct</p>
        <div className="flex flex-wrap justify-center gap-3">
          <Button asChild size="lg"><a href={`#/quizzes/${quizId}/take`} className="no-underline"><RotateCcw aria-hidden="true" />Take again</a></Button>
          <Button asChild variant="outline" size="lg"><a href="#/quizzes" className="no-underline">All quizzes</a></Button>
        </div>
      </CardContent>
    </Card>
  </section>
}
