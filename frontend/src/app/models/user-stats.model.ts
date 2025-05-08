import type { Game } from "./game.model"

export interface UserStats {
  id?: number
  userId: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  gamesDrawn: number
  gamesAbandoned: number
  totalPoints: number
  currentWinStreak: number
  biggestWinStreak: number
  totalFatsCollected: number
}

export interface UserGameStats {
  id?: number
  userId?: number
  gameDefinition: Game
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  gamesDrawn: number
  gamesAbandoned: number
  totalPoints: number
  totalFatsCollected: number
  currentWinStreak: number
  biggestWinStreak: number
  highestScore: number
  highestFatsInGame: number
  lastPlayed?: string
}

export interface GameStatistics {
  id?: number
  userId: string
  gameId: string
  gameDefinitionId: number
  gameType: string
  score: number
  won: boolean
  drawn: boolean
  tricksTaken: number
  fatCardsCollected: number
  playedAt: Date
  friendly: boolean
}

export interface LeaderboardEntry {
  userId: number
  username: string
  gamesPlayed: number
  gamesWon: number
  gamesDrawn: number
  points: number
  winRate: number
  drawRate: number
  gameDefinition?: Game
}
