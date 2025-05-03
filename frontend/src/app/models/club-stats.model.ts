import type { Club } from "./club.model"
import type { Game } from "./game.model"
import type { User } from "./user.model"

export interface ClubStats {
  id: number
  club: Club
  gamesPlayed: number
  totalPoints: number
  totalFatsCollected: number
  uniquePlayersCount: number
  mostActiveGameId: number
}

export interface ClubGameStats {
  id: number
  club: Club
  gameDefinition: Game
  gamesPlayed: number
  totalPoints: number
  highestScore: number
  totalFatsCollected: number
  uniquePlayersCount: number
  lastPlayed: Date
}

export interface ClubMemberStats {
  id: number
  club: Club
  user: User
  gamesPlayed: number
  gamesWon: number
  totalPoints: number
  highestScore: number
  totalFatsCollected: number
  lastPlayed: Date
}
