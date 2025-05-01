import {Game} from './game.model';

export interface UserStats {
  id?: number
  userId: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
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
  tricksTaken: number
  fatCardsCollected: number
  playedAt: Date
}

export interface LeaderboardEntry {
  userId: number
  username: string
  gamesPlayed: number
  gamesWon: number
  points: number
  winRate: number
  gameDefinition?: Game
}
