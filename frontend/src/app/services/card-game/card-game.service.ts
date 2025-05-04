import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import {Observable, BehaviorSubject, Subject, tap, of} from "rxjs"
import { map, catchError } from "rxjs/operators"
import { CardGame, GameAction, GameEvent, Card, Player } from "../../models/card-game.model"
import { AuthService } from "../auth/auth.service"
import {BACKEND_API_URL, IS_DEV} from "../../../environments/api-config"
import {IMessage, Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: "root",
})
export class CardGameService {
  private apiUrl = BACKEND_API_URL + "/card-games"
  private wsUrl = BACKEND_API_URL + "/ws"

  private currentGameSubject = new BehaviorSubject<CardGame | null>(null)
  currentGame$ = this.currentGameSubject.asObservable()

  private gameEventsSubject = new Subject<GameEvent>()
  gameEvents$ = this.gameEventsSubject.asObservable()

  private canHitSubject = new BehaviorSubject<boolean>(false)
  canHit$ = this.canHitSubject.asObservable()

  private lastPlayedCardSubject = new BehaviorSubject<{ card: Card; playerId: string } | null>(null)
  lastPlayedCard$ = this.lastPlayedCardSubject.asObservable()

  private lastStateUpdateTime = 0

  private stompClient: any = null
  private connected = new BehaviorSubject<boolean>(false)
  connected$ = this.connected.asObservable()
  private subscriptions: { [destination: string]: any } = {}
  private connectionAttempts = 0
  private maxConnectionAttempts = 5

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {
    if (IS_DEV) console.log("CardGameService initialized with WebSocket URL:", this.wsUrl)

    this.initializeWebSocketConnection()

    this.connected$.subscribe((connected) => {
      if (connected) {
        const currentGame = this.currentGameSubject.value
        if (currentGame) {
          this.connectToGame(currentGame.id)
        }
      }
    })
  }

  private initializeWebSocketConnection(): void {
    try {
      if (IS_DEV) console.log("Initializing WebSocket connection to:", this.wsUrl)

      const socket = new SockJS(this.wsUrl, null, {
        transports: ["xhr-polling", "xhr-streaming"],
        timeout: 1000,
      })

      this.stompClient = Stomp.over(socket)

      if (IS_DEV) {
        this.stompClient.debug = (str: string) => {
          if (IS_DEV) console.log("STOMP: " + str)
        }
      } else {
        this.stompClient.debug = () => {}
      }

      this.stompClient.connect(
        {},
        (frame: any) => {
          if (IS_DEV) console.log("Connected to WebSocket:", frame)
          this.connected.next(true)
          this.connectionAttempts = 0

          Object.entries(this.subscriptions).forEach(([destination, callback]) => {
            this.subscribeInternal(destination, callback)
          })
        },
        (error: any) => {
          if (IS_DEV) console.error("WebSocket connection error:", error)
          this.handleConnectionError("WebSocket connection error")
        },
      )
    } catch (error) {
      if (IS_DEV) console.error("Error initializing WebSocket:", error)
      this.handleConnectionError("Error initializing WebSocket")
    }
  }

  private handleConnectionError(message: string): void {
    this.connected.next(false)
    this.connectionAttempts++

    if (this.connectionAttempts < this.maxConnectionAttempts) {
      if (IS_DEV)
        console.log(
          `Connection attempt ${this.connectionAttempts}/${this.maxConnectionAttempts} failed. Retrying in 5 seconds...`,
        )
      setTimeout(() => this.initializeWebSocketConnection(), 5000)
    } else {
      if (IS_DEV) console.error(`Failed to connect after ${this.maxConnectionAttempts} attempts. Giving up.`)
    }
  }

  subscribe(destination: string, callback: (message: any) => void): void {
    if (IS_DEV) console.log(`Subscribing to ${destination}`)
    this.subscriptions[destination] = callback

    if (this.isConnected()) {
      this.subscribeInternal(destination, callback)
    } else {
      if (IS_DEV) console.log(`Not connected yet. Will subscribe to ${destination} when connected.`)
    }
  }

  private subscribeInternal(destination: string, callback: (message: any) => void): any {
    if (IS_DEV) console.log(`Actually subscribing to ${destination}`)
    return this.stompClient.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body)
        callback(body)
      } catch (error) {
        if (IS_DEV) console.error(`Error processing message from ${destination}:`, error)
      }
    })
  }

  unsubscribe(destination: string): void {
    if (IS_DEV) console.log(`Unsubscribing from ${destination}`)
    if (this.subscriptions[destination]) {
      if (this.isConnected() && this.subscriptions[destination].unsubscribe) {
        this.subscriptions[destination].unsubscribe()
      }
      delete this.subscriptions[destination]
    }
  }

  send(destination: string, body: any): void {
    if (this.isConnected()) {
      if (IS_DEV) console.log(`Sending message to ${destination}:`, body)
      this.stompClient.send(destination, {}, JSON.stringify(body))
    } else {
      if (IS_DEV) console.error(`Cannot send message to ${destination}, not connected to WebSocket`)
    }
  }

  isConnected(): boolean {
    return this.stompClient !== null && this.stompClient.connected
  }

  disconnect(): void {
    if (this.stompClient) {
      if (IS_DEV) console.log("Disconnecting WebSocket client")
      this.stompClient.disconnect()
      this.stompClient = null
      this.connected.next(false)
      this.subscriptions = {}
    }
  }

  reconnect(): void {
    if (IS_DEV) console.log("Manually reconnecting WebSocket")
    this.disconnect()
    this.connectionAttempts = 0
    this.initializeWebSocketConnection()
  }

  connectToGame(gameId: string): void {
    if (IS_DEV) console.log(`Connecting to game: ${gameId}`)

    const gameTopic = `/topic/game/${gameId}`

    this.subscribe(gameTopic, (event: GameEvent) => {
      if (IS_DEV) console.log("Received game event:", event)
      this.gameEventsSubject.next(event)

      if (event.eventType === "GAME_ACTION") {
        this.forceRefreshGame(gameId)
      } else {
        this.getGame(gameId).subscribe({
          next: (game) => {
            if (IS_DEV) console.log("Game updated after event:", event.eventType)
          },
          error: (err) => {
            if (IS_DEV) console.error("Error updating game after event:", err)
          },
        })
      }
    })

    if (this.isConnected()) {
      this.joinGame(gameId)
    } else {
      if (IS_DEV) console.log("WebSocket not connected, waiting for connection...")
      const subscription = this.connected$.subscribe((connected) => {
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
      this.send("/app/game.join", { gameId, userId })
    } else {
      if (IS_DEV) console.error("Cannot join game: No user ID available")
    }
  }

  disconnectFromGame(gameId: string): void {
    const gameTopic = `/topic/game/${gameId}`
    this.unsubscribe(gameTopic)

    const userId = this.authService.currentUser?.id.toString()
    if (userId && this.isConnected()) {
      this.send("/app/game.leave", { gameId, userId })
    }
  }

  abandonGame(gameId: string): Observable<any> {
    const userId = this.authService.currentUser?.id.toString()

    if (userId && this.isConnected()) {
      this.send("/app/game.abandon", { gameId, userId })
      this.clearCurrentGame()
      return of(null)
    } else {
      return this.http.post<any>(`${this.apiUrl}/${gameId}/abandon`, {}).pipe(
        tap(() => {
          this.clearCurrentGame()
        }),
      )
    }
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
    if (IS_DEV) console.log("Processing game state for game " + game.id + ": ", JSON.stringify(game.gameState))
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
    if (userId) {
      if (IS_DEV) console.log("Executing action:", action)

      const actionTime = Date.now()

      if (this.isConnected()) {
        this.send("/app/game.action", {
          gameId,
          userId,
          action,
        })
      } else {
        this.http
          .post<CardGame>(`${this.apiUrl}/${gameId}/action`, {
            userId,
            action,
          })
          .subscribe({
            next: (game) => {
              this.currentGameSubject.next(this.processGameState(game))
            },
            error: (err) => {
              if (IS_DEV) console.error("Error executing action via HTTP:", err)
            },
          })
      }

      setTimeout(() => {
        if (actionTime > this.lastStateUpdateTime) {
          this.forceRefreshGame(gameId)
        }
      }, 500)
    } else {
      if (IS_DEV) console.error("Cannot execute action: No user ID available")
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
