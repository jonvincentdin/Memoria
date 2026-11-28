import { RefreshCw } from 'lucide-react'
import { useResource } from '../../shared/api/use-resource'
import { dateTime } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import { Back, Empty, Failure, Loading, PageTitle } from '../../shared/ui/common'
import type { Reviewer } from '../reviewers/types'
import { DueOverview } from './DueOverview'
import { scopeQuery } from './query'
import type { ProgressSummary, ReviewHistoryItem } from './types'

const ratings = ['Again', 'Hard', 'Okay', 'Good', 'Easy']

export function ProgressPage({ reviewerId }: { reviewerId?: string }) {
  const summary = useResource<ProgressSummary>('/api/flashcards/progress' + scopeQuery(reviewerId))
  const history = useResource<ReviewHistoryItem[]>('/api/flashcards/reviews' + scopeQuery(reviewerId))
  const reviewers = useResource<Reviewer[]>('/api/reviewers')
  const refresh = () => { summary.reload(); history.reload(); reviewers.reload() }
  const reviewer = reviewers.data?.find(item => item.id === reviewerId)
  const error = summary.error || history.error || reviewers.error
  const loading = summary.loading || history.loading || reviewers.loading
  const data = summary.data
  const mastery = data?.totalCards ? Math.round(data.masteredCards / data.totalCards * 100) : 0
  return <>
    <Back href={`#/flashcards${reviewerId ? `/reviewer/${reviewerId}` : ''}`}>Back to flashcards</Back>
    <PageTitle title="Your recall, over time" description={reviewer ? `Progress and saved reviews for ${reviewer.title}.` : 'Progress and saved reviews across your flashcards.'} actions={<Button variant="outline" onClick={refresh} disabled={loading}><RefreshCw aria-hidden="true" />Refresh progress</Button>} />
    <div className="field progress-filter"><label htmlFor="progress-reviewer">Show progress for</label><select id="progress-reviewer" className="select" value={reviewerId || ''} onChange={event => { window.location.hash = `#/flashcards/progress${event.target.value ? `/${event.target.value}` : ''}` }}><option value="">All reviewers</option>{reviewers.data?.map(item => <option key={item.id} value={item.id}>{item.title}</option>)}</select></div>
    {loading ? <Loading label="Loading your progress…" /> : error ? <Failure error={error} retry={refresh} /> : data && <>
      <DueOverview summary={data} reviewerId={reviewerId} />
      <section aria-labelledby="progress-heading"><h2 id="progress-heading">Your card collection</h2><div className="stats">{[['total', data.totalCards, 'Total cards'], ['new', data.newCards, 'New'], ['learning', data.learningCards, 'Learning'], ['mastered', data.masteredCards, 'Mastered']].map(([key, count, label]) => <div key={key} data-progress={key}><span className="stat-value">{count}</span><span className="stat-label">{label}</span></div>)}</div>
        {!!data.totalCards && <div className="study-progress mastery-progress"><div className="section-heading"><span className="field-label">Mastered cards</span><span className="tabular">{mastery}%</span></div><progress value={data.masteredCards} max={data.totalCards} aria-label="Mastered cards" /><p className="muted">A card becomes mastered after three consecutive ratings of 3 or higher. Ratings of 1 or 2 reset that streak.</p></div>}
      </section>
      <section className="section-gap" aria-labelledby="review-totals-heading"><h2 id="review-totals-heading">Study activity</h2><div className="stats activity-stats"><div><span className="stat-value" data-progress="reviews">{data.totalReviews}</span><span className="stat-label">Saved reviews</span></div><div><span className="stat-value">{data.reviewsToday}</span><span className="stat-label">Reviews today (UTC)</span></div><div><span className="stat-value">{data.averageRating === null ? '—' : data.averageRating.toFixed(1)}</span><span className="stat-label">Average rating / 5</span></div></div>{data.lastReviewedAt && <p className="meta">Last studied: {dateTime(data.lastReviewedAt)}</p>}</section>
      <section className="section-gap" aria-labelledby="history-heading"><div className="section-heading"><h2 id="history-heading">Review history</h2><span className="meta">Most recent first</span></div>{!history.data?.length ? <Empty title="Your first review starts the story" action={<Button asChild><a href={`#/flashcards${reviewerId ? `/reviewer/${reviewerId}` : ''}`}>Go to flashcards</a></Button>}>Reveal a card’s answer and rate your recall. Each saved review will appear here.</Empty> : <ol className="review-history">{history.data.map(entry => <li key={entry.id}><div className="history-copy"><p className="meta"><time dateTime={`${entry.reviewedAt}Z`}>{dateTime(entry.reviewedAt)}</time><span>{entry.rating} / 5 · {ratings[entry.rating - 1]}</span></p><h3>{entry.front}</h3><a href={`#/reviewers/${entry.reviewerId}`}>{entry.reviewerTitle}</a></div><p className="history-schedule"><span>Next review: {dateTime(entry.nextReviewAt)}</span><span>{entry.intervalDays}-day interval</span></p></li>)}</ol>}</section>
    </>}
  </>
}
