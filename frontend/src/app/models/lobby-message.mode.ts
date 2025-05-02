import type { User } from "./user.model"

export interface LobbyMessage {
  id: number
  content: string
  user: User
  lobbyId?: number
  gameId?: string
  isLobbyMessage?: boolean
  timestamp: Date
  status?: "active" | "unsent" | "removed"
}
