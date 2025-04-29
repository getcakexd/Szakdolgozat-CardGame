import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable, BehaviorSubject, Subject } from "rxjs"
import { map, catchError } from "rxjs/operators"
import { CardGame, GameAction, GameEvent, Card, Player } from "../../models/card-game.model"
import { WebSocketService } from "../websocket/websocket.service"
import { AuthService } from "../auth/auth.service"
import { BACKEND_API_URL } from "../../../environments/api-config"

@Injectable({
  providedIn: "root",
})
export class CardGameService {
  private apiUrl = BACKEND_API_URL + "/card-games"
  private currentGameSubject = new BehaviorSubject<CardGame | null>(null)
  currentGame$ = this.currentGameSubject.asObservable()

  private gameEventsSubject = new Subject<GameEvent>()
  gameEvents$ = this.gameEventsSubject.asObservable()

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService,
    private authService: AuthService,
  ) {
    this.webSocketService.connected$.subscribe((connected) => {
      if (connected) {
        const currentGame = this.currentGameSubject.value
        if (currentGame) {
          this.connectToGame(currentGame.id)
        }
      }
    })
  }

  connectToGame(gameId: string): void {
    console.log(`Connecting to game: ${gameId}`)

    this.webSocketService.subscribe(`/topic/game/${gameId}`, (event: GameEvent) => {
      console.log("Received game event:", event)
      this.gameEventsSubject.next(event)

      this.getGame(gameId).subscribe({
        next: (game) => {
          console.log("Game updated after event:", event.type)
        },
        error: (err) => console.error("Error updating game after event:", err),
      })
    })

    if (this.webSocketService.isConnected()) {
      this.joinGame(gameId)
    } else {
      console.log("WebSocket not connected, waiting for connection...")
      const subscription = this.webSocketService.connected$.subscribe((connected) => {
        if (connected) {
          this.joinGame(gameId)
          subscription.unsubscribe()
        }
      })
    }
  }

  private joinGame(gameId: string): void {
    const userId = this.authService.currentUser?.id.toString()
    if (userId) {
      console.log(`Joining game ${gameId} as user ${userId}`)
      this.webSocketService.send("/app/game.join", { gameId, userId })
    } else {
      console.error("Cannot join game: No user ID available")
    }
  }

  disconnectFromGame(gameId: string): void {
    this.webSocketService.unsubscribe(`/topic/game/${gameId}`)

    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.webSocketService.isConnected()) {
      this.webSocketService.send("/app/game.leave", { gameId, userId })
    }
  }

  getGame(gameId: string): Observable<CardGame> {
    console.log(`Fetching game data from: ${this.apiUrl}/${gameId}`)
    return this.http.get<CardGame>(`${this.apiUrl}/${gameId}`).pipe(
      map((game) => {
        console.log("Received game data:", game)
        game.createdAt = new Date(game.createdAt)
        if (game.startedAt) game.startedAt = new Date(game.startedAt)
        if (game.endedAt) game.endedAt = new Date(game.endedAt)

        this.currentGameSubject.next(game)
        return game
      }),
      catchError((error) => {
        console.error("Error fetching game:", error)
        throw error
      }),
    )
  }

  executeAction(gameId: string, action: GameAction): void {
    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.webSocketService.isConnected()) {
      this.webSocketService.send("/app/game.action", {
        gameId,
        userId,
        action
      })

    } else {
      console.error("Cannot execute action: WebSocket not connected or no user ID")
    }
  }

  playCard(gameId: string, card: Card): void {
    const action: GameAction = {
      actionType: "playCard",
      parameters: { card },
    }
    this.executeAction(gameId,  action)
  }

  sendPartnerMessage(gameId: string, messageType: string, content: string): void {
    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.webSocketService.isConnected()) {
      this.webSocketService.send("/app/game.message", {
        gameId,
        userId,
        messageType: "PARTNER_MESSAGE",
        content,
      })
    } else {
      console.error("Cannot send partner message: WebSocket not connected or no user ID")
    }
  }

  getCurrentPlayer(): Player | undefined {
    const game = this.currentGameSubject.value
    const userId = this.authService.currentUser?.id.toString()

    if (!game || !userId) return undefined

    return game.players.find((player) => player.id === userId)
  }

  isCurrentPlayerTurn(): boolean {
    const game = this.currentGameSubject.value
    const userId = this.authService.currentUser?.id.toString()

    if (!game || !userId || !game.currentPlayer) return false

    return game.currentPlayer.id === userId
  }

  clearCurrentGame(): void {
    this.currentGameSubject.next(null)
  }
}
