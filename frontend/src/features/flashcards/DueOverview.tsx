import { CalendarClock } from 'lucide-react'
import { dateTime } from '../../shared/format'
import { Button } from '../../shared/ui/button'
import type { ProgressSummary } from './types'

export function DueOverview({ summary, reviewerId }: { summary: ProgressSummary; reviewerId?: string }) {
  return <section className="due-overview" aria-label="Due flashcards">
    <div><p className="field-label">Ready for a little recall?</p><h2>{summary.dueCards} {summary.dueCards === 1 ? 'card' : 'cards'} due</h2><p className="muted">{summary.dueCards ? 'New and scheduled cards, ready to study now.' : summary.totalCards ? summary.nextDueAt ? `Next review: ${dateTime(summary.nextDueAt)}.` : 'Your scheduled cards are up to date.' : 'Create your first card to start a review schedule.'}</p></div>
    {!!summary.totalCards && <Button asChild><a href={`#/flashcards/due${reviewerId ? `/${reviewerId}` : ''}`}><CalendarClock aria-hidden="true" />Review due cards</a></Button>}
  </section>
}
