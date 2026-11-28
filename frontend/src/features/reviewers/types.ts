export type LinkedNote = { id: string; title: string }
export type Reviewer = { id: string; title: string; style: string; content: string; createdAt: string; notes: LinkedNote[] }
