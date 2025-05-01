export interface UserStats {
  id?: number
  userId: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  gamesAbandoned: number
  points: number
  currentWinStreak: number
  biggestWinStreak: number
  totalFatsCollected: number
}

export interface UserGameStats {
  id?: number
  userId: number
  gameId: number
  gameName: string
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  gamesAbandoned: number
  points: number
  fatsCollected: number
  currentWinStreak: number
  biggestWinStreak: number
  highestScore: number
  mostFatsInGame: number
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
}

export interface Game {
  id: number
  name: string
  active: boolean
  minPlayers: number
  maxPlayers: number
  factorySign: string
  factoryId: number
  descriptions?: any[]
  rules?: any[]
}
