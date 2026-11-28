import { ArrowRight, RefreshCw } from 'lucide-react'
import { useState } from 'react'
import type { User } from '../auth/types'
import { useResource } from '../../shared/api/use-resource'
import { dateOnly } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Loading, Notice, PageTitle } from '../../shared/ui/common'
import { navigation } from '../../app/navigation'
import { DueOverview } from '../flashcards/DueOverview'
import type { ProgressSummary } from '../flashcards/types'

export function Dashboard({ user }: { user: User }) {
  const notes = useResource<unknown[]>('/api/notes')
  const reviewers = useResource<unknown[]>('/api/reviewers')
  const quizzes = useResource<unknown[]>('/api/quizzes')
  const progress = useResource<ProgressSummary>('/api/flashcards/progress')
  const [hour] = useState(() => new Date().getHours())
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'
  const error = notes.error || reviewers.error || quizzes.error
  const loading = notes.loading || reviewers.loading || quizzes.loading
  const tools = navigation.filter(item => item.path !== 'dashboard')
  return <>
    <PageTitle title={`${greeting}, ${user.name.trim().split(/\s+/)[0] || 'learner'}.`} description="A little space to collect your thoughts and keep learning." actions={<Button variant="outline" onClick={() => { notes.reload(); reviewers.reload(); quizzes.reload(); progress.reload() }} disabled={loading}><RefreshCw aria-hidden="true" />Refresh overview</Button>} />
    <div className="dashboard-layout"><section><h2>Your study collection</h2>{loading ? <Loading label="Loading your collection…" /> : error ? <Notice error={error} /> : <><div className="stats dashboard-stats">{[['notes', notes.data?.length || 0, 'Active notes'], ['reviewers', reviewers.data?.length || 0, 'Reviewers'], ['quizzes', quizzes.data?.length || 0, 'Practice quizzes']].map(([key, count, label]) => <div key={key} data-stat={key}><span className="stat-value">{count}</span><span className="stat-label">{label}</span></div>)}</div>{!notes.data?.length && !reviewers.data?.length && !quizzes.data?.length && <p className="muted">A new page, ready for what you learn next.</p>}</>}
      <section className="section-gap"><h2>Flashcard practice</h2>{progress.loading ? <Loading label="Loading due cards…" /> : progress.error ? <Notice error={progress.error} /> : progress.data && <DueOverview summary={progress.data} />}<a href="#/flashcards/progress">View progress and review history</a></section>
      {!!tools.length && <section className="section-gap"><h2>Keep learning</h2><ul className="resource-list">{tools.map(({ path, label, icon: Icon }) => <li key={path}><a className="resource-row" href={`#/${path}`}><span className="actions"><Icon size={19} aria-hidden="true" />{label}</span><ArrowRight size={18} aria-hidden="true" /></a></li>)}</ul></section>}
    </section><aside className="paper-panel account-details"><h2>Your account</h2><dl><div><dt>Name</dt><dd>{user.name}</dd></div><div><dt>Email</dt><dd>{user.email}</dd></div><div><dt>Learning here since</dt><dd>{dateOnly(user.createdAt)}</dd></div></dl></aside></div>
  </>
}
