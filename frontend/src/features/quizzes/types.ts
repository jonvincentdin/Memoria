export type QuizQuestion = { text: string; choices: string[]; correctIndex: number }
export type Quiz = { id: string; title: string; mode: string; questions: QuizQuestion[]; createdAt: string; hasAttempts: boolean; reviewers: { id: string; title: string }[] }
export type TakingQuiz = { title: string; questions: { text: string; choices: string[] }[] }
export type Attempt = { id: string; quizId: string; quizTitle: string; score: number; totalQuestions: number; percentage: number; status: string; completedAt: string }
