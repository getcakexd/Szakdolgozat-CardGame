import type { User } from "./user.model"

export interface AuditLog {
  id: number
  action: string
  user: User
  userId: number
  timestamp: Date
  details: string
}

export interface AuditLogFilter {
  userId?: number
  action?: string
  startDate?: Date
  endDate?: Date
}
