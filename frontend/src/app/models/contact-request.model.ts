export interface ContactRequest {
  id?: number
  name: string
  email: string
  subject: string
  message: string
  status: "new" | "in-progress" | "resolved"
  createdAt: Date
  assignedTo?: number
}
