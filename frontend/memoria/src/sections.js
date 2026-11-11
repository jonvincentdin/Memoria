import { ChartColumn, ClipboardList, NotebookPen, Target } from 'lucide-react'

// Main study features. `ready: false` routes render the placeholder page until the screen is built.
export const SECTIONS = [
  { route: 'notes', label: 'Notes', icon: NotebookPen, description: 'Write and organize what you learn.', ready: false },
  { route: 'reviewers', label: 'Reviewers', icon: ClipboardList, description: 'Turn notes into quick recaps.', ready: false },
  { route: 'quizzes', label: 'Quizzes', icon: Target, description: 'Practice with questions from your notes.', ready: false },
  { route: 'results', label: 'Results', icon: ChartColumn, description: 'See how your practice is going.', ready: false },
]
