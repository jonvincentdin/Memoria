export type QuizQuestion = { text: string; choices: string[]; correctIndex: number }
export type Quiz = { id: string; title: string; mode: string; questions: QuizQuestion[]; createdAt: string; hasAttempts: boolean; reviewers: { id: string; title: string }[] }
