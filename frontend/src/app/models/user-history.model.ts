export interface UserHistory {
  id: number
  userId: number
  previousUsername?: string
  previousEmail?: string
  changedAt: Date
  changedBy: string
}
