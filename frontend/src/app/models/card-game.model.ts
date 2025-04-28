export enum CardSuit {
  HEARTS = "HEARTS",
  DIAMONDS = "BELLS",
  CLUBS = "CLUBS",
  SPADES = "SPADES",
}

export enum CardRank {
  SEVEN = "SEVEN",
  EIGHT = "EIGHT",
  NINE = "NINE",
  TEN = "TEN",
  UNDER = "UNDER",
  OVER = "OVER",
  KING = "KING",
  ACE = "ACE",
}

export interface Card {
  suit: CardSuit
  rank: CardRank
  value?: number
}

export enum GameStatus {
  WAITING = "WAITING",
  ACTIVE = "ACTIVE",
  FINISHED = "FINISHED",
}

export interface Player {
  id: string
  username: string
  hand: Card[]
  wonCards: Card[]
  active: boolean
  score: number
}

export interface GameAction {
  actionType: string
  parameters: { [key: string]: any }
}

export interface GameEvent {
  type: string
  gameId: string
  playerId?: string
  timestamp?: Date
  data?: { [key: string]: any }
}

export interface CardGame {
  id: string
  gameDefinitionId: number
  name: string
  players: Player[]
  status: GameStatus
  createdAt: Date
  startedAt?: Date
  endedAt?: Date
  currentPlayer?: Player
  trackStatistics: boolean
  gameState: { [key: string]: any }
}

export interface PartnerMessage {
  type: string
  content: string
}

export const PARTNER_MESSAGE_TYPES = {
  CALL_PARTNER: "CALL_PARTNER",
  PASS: "PASS",
  TAKE: "TAKE",
  LEAVE: "LEAVE",
}
