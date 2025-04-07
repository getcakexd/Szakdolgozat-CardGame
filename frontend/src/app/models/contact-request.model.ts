export interface ContactRequest {
  id?: number
  name: string
  email: string
  problemType: "locked-account" | "stolen-account" | "report-user" | "other"
  subject: string
  message: string
  status: "new" | "in-progress" | "resolved"
  createdAt: Date
  assignedTo?: number
}

