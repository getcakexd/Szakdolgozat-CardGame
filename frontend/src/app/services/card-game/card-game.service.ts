import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import {Observable, BehaviorSubject, Subject, tap} from "rxjs"
import { map, catchError } from "rxjs/operators"
import { CardGame, GameAction, GameEvent, Card, Player } from "../../models/card-game.model"
import { WebSocketService } from "../websocket/websocket.service"
import { AuthService } from "../auth/auth.service"
import {BACKEND_API_URL, IS_DEV} from "../../../environments/api-config"

@Injectable({
  providedIn: "root",
})
export class CardGameService {
  private apiUrl = BACKEND_API_URL + "/card-games"
  private currentGameSubject = new BehaviorSubject<CardGame | null>(null)
  currentGame$ = this.currentGameSubject.asObservable()

  private gameEventsSubject = new Subject<GameEvent>()
  gameEvents$ = this.gameEventsSubject.asObservable()

  private canHitSubject = new BehaviorSubject<boolean>(false)
  canHit$ = this.canHitSubject.asObservable()

  private lastPlayedCardSubject = new BehaviorSubject<{ card: Card; playerId: string } | null>(null)
  lastPlayedCard$ = this.lastPlayedCardSubject.asObservable()

  private lastStateUpdateTime = 0

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
    if (IS_DEV) console.log(`Connecting to game: ${gameId}`)

    this.webSocketService.subscribe(`/topic/game/${gameId}`, (event: GameEvent) => {
      if (IS_DEV) console.log("Received game event:", event)
      this.gameEventsSubject.next(event)

      if (event.type === "GAME_ACTION") {
        this.forceRefreshGame(gameId)
      } else {
        this.getGame(gameId).subscribe({
          next: (game) => {
            if (IS_DEV) console.log("Game updated after event:", event.type)
          },
          error: (err) => {
            if (IS_DEV) console.error("Error updating game after event:", err)
          },
        })
      }
    })

    if (this.webSocketService.isConnected()) {
      this.joinGame(gameId)
    } else {
      if (IS_DEV) console.log("WebSocket not connected, waiting for connection...")
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
      if (IS_DEV) console.log(`Joining game ${gameId} as user ${userId}`)
      this.webSocketService.send("/app/game.join", { gameId, userId })
    } else {
      if (IS_DEV) console.error("Cannot join game: No user ID available")
    }
  }

  disconnectFromGame(gameId: string): void {
    this.webSocketService.unsubscribe(`/topic/game/${gameId}`)

    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.webSocketService.isConnected()) {
      this.webSocketService.send("/app/game.leave", { gameId, userId })
    }
  }

  abandonGame(gameId: string): Observable<any> {
    const userId = this.authService.currentUser?.id.toString()

    if (userId && this.webSocketService.isConnected()) {
      this.webSocketService.send("/app/game.abandon", { gameId, userId })
    }

    return this.http.post<any>(`${this.apiUrl}/${gameId}/abandon`, {}).pipe(
      tap(() => {
        this.clearCurrentGame()
      }),
    )
  }

  forceRefreshGame(gameId: string): void {
    if (IS_DEV) console.log("Forcing complete game refresh")

    setTimeout(() => {
      this.http
        .get<CardGame>(`${this.apiUrl}/${gameId}`)
        .pipe(
          map((game) => this.processGameState(game)),
          tap((game) => {
            if (IS_DEV) console.log("Forced refresh complete, new game state:", game)

            this.lastStateUpdateTime = Date.now()

            this.currentGameSubject.next(game)

            const canHit = game.gameState && game.gameState["canHit"] === true
            this.canHitSubject.next(canHit)

            if (game.gameState && game.gameState["lastPlayedCard"] && game.gameState["lastPlayer"]) {
              this.lastPlayedCardSubject.next({
                card: game.gameState["lastPlayedCard"],
                playerId: game.gameState["lastPlayer"],
              })
            }
          }),
          catchError((error) => {
            if (IS_DEV) console.error("Error in forced refresh:", error)
            throw error
          }),
        )
        .subscribe()
    }, 300)
  }

  getGame(gameId: string): Observable<CardGame> {
    if (IS_DEV) console.log(`Fetching game data from: ${this.apiUrl}/${gameId}`)
    return this.http.get<CardGame>(`${this.apiUrl}/${gameId}`).pipe(
      map((game) => {
        if (IS_DEV) console.log("Received game data:", game)

        game.createdAt = new Date(game.createdAt)
        if (game.startedAt) game.startedAt = new Date(game.startedAt)
        if (game.endedAt) game.endedAt = new Date(game.endedAt)

        const processedGame = this.processGameState(game)

        this.lastStateUpdateTime = Date.now()

        const canHit = processedGame.gameState && processedGame.gameState["canHit"] === true
        this.canHitSubject.next(canHit)

        if (
          processedGame.gameState &&
          processedGame.gameState["lastPlayedCard"] &&
          processedGame.gameState["lastPlayer"]
        ) {
          this.lastPlayedCardSubject.next({
            card: processedGame.gameState["lastPlayedCard"],
            playerId: processedGame.gameState["lastPlayer"],
          })
        }

        this.currentGameSubject.next(processedGame)
        return processedGame
      }),
      catchError((error) => {
        if (IS_DEV) console.error("Error fetching game:", error)
        throw error
      }),
    )
  }

  private processGameState(game: CardGame): CardGame {
    if (IS_DEV) console.log("Processing game state:", JSON.stringify(game.gameState))

    const processedGame = JSON.parse(JSON.stringify(game))

    if (!processedGame.gameState) {
      processedGame.gameState = {}
    }

    if (!processedGame.gameState["currentTrick"]) {
      processedGame.gameState["currentTrick"] = []
    }

    if (!Array.isArray(processedGame.gameState["currentTrick"])) {
      processedGame.gameState["currentTrick"] = []
    }

    if (processedGame.gameState["currentTrick"].length > 0) {
      processedGame.gameState["currentTrick"] = processedGame.gameState["currentTrick"].filter(
        (card: Card) => card && typeof card === "object" && card.suit && card.rank,
      )
    }

    if (IS_DEV) console.log("Processed game state:", processedGame.gameState)
    return processedGame
  }

  executeAction(gameId: string, action: GameAction): void {
    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.webSocketService.isConnected()) {
      if (IS_DEV) console.log("Executing action:", action)

      const actionTime = Date.now()

      this.webSocketService.send("/app/game.action", {
        gameId,
        userId,
        action,
      })

      setTimeout(() => {
        if (actionTime > this.lastStateUpdateTime) {
          this.forceRefreshGame(gameId)
        }
      }, 500)
    } else {
      if (IS_DEV) console.error("Cannot execute action: WebSocket not connected or no user ID")
    }
  }

  playCard(gameId: string, card: Card): void {
    const action: GameAction = {
      actionType: "playCard",
      parameters: { card },
    }
    this.executeAction(gameId, action)
  }

  pass(gameId: string): void {
    const action: GameAction = {
      actionType: "pass",
      parameters: {},
    }
    this.executeAction(gameId, action)
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

  canCurrentPlayerHit(): boolean {
    return this.canHitSubject.value
  }

  clearCurrentGame(): void {
    this.currentGameSubject.next(null)
  }
}
