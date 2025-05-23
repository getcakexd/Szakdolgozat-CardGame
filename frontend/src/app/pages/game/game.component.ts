import { Component, OnInit, OnDestroy, ChangeDetectorRef, HostListener } from "@angular/core"
import { ActivatedRoute, Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Subscription } from "rxjs"
import { CardGameService } from "../../services/card-game/card-game.service"
import { AuthService } from "../../services/auth/auth.service"
import { CardGame, GameStatus, Card, Player } from "../../models/card-game.model"
import { CommonModule } from "@angular/common"
import { MatButtonModule } from "@angular/material/button"
import { MatCardModule } from "@angular/material/card"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { MatTooltipModule } from "@angular/material/tooltip"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { RouterModule } from "@angular/router"
import { CardComponent } from "../../components/card/card.component"
import { PlayerInfoComponent } from "../../components/player-info/player-info.component"
import { IS_DEV } from "../../../environments/api-config"
import { LobbyService } from "../../services/lobby/lobby.service"
import { LobbyChatComponent } from "../../components/lobby-chat/lobby-chat.component"
import { LOBBY_STATUS } from "../../models/lobby.model"

@Component({
  selector: "app-game",
  templateUrl: "./game.component.html",
  styleUrls: ["./game.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    TranslateModule,
    CardComponent,
    PlayerInfoComponent,
    RouterModule,
    LobbyChatComponent,
  ],
})
export class GameComponent implements OnInit, OnDestroy {
  game: CardGame | null = null
  currentPlayer: Player | null = null
  isLoading = true
  gameStatus = GameStatus
  selectedCard: Card | null = null
  lastActionTime = 0
  canHit = false
  lastPlayedCard: { card: Card; playerId: string } | null = null
  showLastPlayedCard = false
  gameResult: "win" | "loss" | "draw" | null = null
  gameResultMessage = ""
  gameResultSubtitle = ""
  isMobile = false;

  private lastTrickSize = 0
  private screenWidth = window.innerWidth;

  protected gameId: string | null = null
  private gameSubscription: Subscription | null = null
  private eventsSubscription: Subscription | null = null
  private canHitSubscription: Subscription | null = null
  private lastPlayedCardSubscription: Subscription | null = null
  private refreshInterval: any = null
  private lastCardTimer: any = null
  private connectionSubscription: Subscription | null = null

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    this.screenWidth = event.target.innerWidth;
    this.checkIfMobile();
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cardGameService: CardGameService,
    private authService: AuthService,
    private lobbyService: LobbyService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private changeDetectorRef: ChangeDetectorRef,
  ) {
    this.isMobile = window.innerWidth < 768;
  }

  private checkIfMobile() {
    this.isMobile = this.screenWidth < 768;
  }

  ngOnInit(): void {
    this.checkIfMobile();

    if (!this.authService.currentUser) {
      this.snackBar.open(this.translate.instant("ERRORS.LOGIN_REQUIRED"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
      this.router.navigate(["/login"])
      return
    }

    this.gameId = this.route.snapshot.paramMap.get("id")
    if (!this.gameId) {
      this.snackBar.open(this.translate.instant("GAME.INVALID_ID"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
      this.router.navigate(["/lobby"])
      return
    }

    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible" && this.gameId) {
        if (!this.cardGameService.isConnected()) {
          if (IS_DEV) console.log("Page became visible, reconnecting and refreshing game...")
          this.cardGameService.reconnect()
          setTimeout(() => {
            if (this.gameId) {
              this.cardGameService.forceRefreshGame(this.gameId)
            }
          }, 1000)
        }
      }
    })

    if (!this.cardGameService.isConnected()) {
      if (IS_DEV) console.log("WebSocket not connected. Attempting to reconnect...")
      this.cardGameService.reconnect()

      this.snackBar.open(this.translate.instant("GAME.CONNECTING"), this.translate.instant("COMMON.CLOSE"), {
        duration: 5000,
      })

      this.connectionSubscription = this.cardGameService.connected$.subscribe((connected) => {
        if (connected) {
          this.loadGame()
        }
      })

      setTimeout(() => {
        if (!this.cardGameService.isConnected()) {
          if (this.connectionSubscription) {
            this.connectionSubscription.unsubscribe()
            this.connectionSubscription = null
          }

          this.loadGame()

          this.snackBar.open(this.translate.instant("GAME.CONNECTION_FAILED"), this.translate.instant("COMMON.CLOSE"), {
            duration: 3000,
          })
        }
      }, 10000)
    } else {
      this.loadGame()
    }

    this.refreshInterval = setInterval(() => {
      if (this.gameId && this.game?.status === GameStatus.ACTIVE) {
        const now = Date.now()
        if (now - this.lastActionTime > 10000) {
          if (IS_DEV) console.log("Performing periodic refresh")
          this.cardGameService.forceRefreshGame(this.gameId)
        }
      }
    }, 30000)
  }

  ngOnDestroy(): void {
    if (this.gameSubscription) {
      this.gameSubscription.unsubscribe()
    }

    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe()
    }

    if (this.canHitSubscription) {
      this.canHitSubscription.unsubscribe()
    }

    if (this.lastPlayedCardSubscription) {
      this.lastPlayedCardSubscription.unsubscribe()
    }

    if (this.connectionSubscription) {
      this.connectionSubscription.unsubscribe()
    }

    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }

    if (this.lastCardTimer) {
      clearTimeout(this.lastCardTimer)
    }

    document.removeEventListener("visibilitychange", () => {})
  }

  loadGame(): void {
    if (!this.gameId) return

    this.isLoading = true

    this.gameSubscription = this.cardGameService.currentGame$.subscribe((game) => {
      if (IS_DEV) console.log("Game state updated in component:", game?.gameState)

      const currentTrickSize =
        game?.gameState && Array.isArray(game.gameState["currentTrick"]) ? game.gameState["currentTrick"].length : 0
      const trickChanged = this.lastTrickSize !== currentTrickSize

      this.game = game

      if (game) {
        const userId = this.authService.currentUser?.id.toString()
        this.currentPlayer = game.players.find((p) => p.id === userId) || null

        this.lastTrickSize = currentTrickSize

        if (trickChanged && currentTrickSize === 0 && this.lastTrickSize > 0) {
          this.showTrickCompletedNotification()
        }

        if (game.status === GameStatus.FINISHED) {
          this.determineGameResult()
        } else {
          this.gameResult = null
          this.gameResultMessage = ""
          this.gameResultSubtitle = ""
        }
      }

      this.isLoading = false
      this.changeDetectorRef.detectChanges()
    })

    this.canHitSubscription = this.cardGameService.canHit$.subscribe((canHit) => {
      this.canHit = canHit
      this.changeDetectorRef.detectChanges()
    })

    this.lastPlayedCardSubscription = this.cardGameService.lastPlayedCard$.subscribe((lastPlayed) => {
      if (
        lastPlayed &&
        (!this.lastPlayedCard ||
          this.lastPlayedCard.card.suit !== lastPlayed.card.suit ||
          this.lastPlayedCard.card.rank !== lastPlayed.card.rank ||
          this.lastPlayedCard.playerId !== lastPlayed.playerId)
      ) {
        this.lastPlayedCard = lastPlayed
        this.showLastPlayedCard = true

        if (this.lastCardTimer) {
          clearTimeout(this.lastCardTimer)
        }

        this.lastCardTimer = setTimeout(() => {
          this.showLastPlayedCard = false
          this.changeDetectorRef.detectChanges()
        }, 3000)

        this.changeDetectorRef.detectChanges()
      }
    })

    this.eventsSubscription = this.cardGameService.gameEvents$.subscribe((event) => {
      if (!event.timestamp) {
        event.timestamp = new Date()
      }

      this.lastActionTime = Date.now()

      if (event.eventType === "GAME_STARTED") {
        this.snackBar.open(this.translate.instant("GAME.GAME_STARTED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      } else if (event.eventType === "GAME_OVER") {
        this.snackBar.open(this.translate.instant("GAME.GAME_OVER"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        if (this.gameId) {
          setTimeout(() => this.cardGameService.forceRefreshGame(this.gameId!), 500)
        }
      } else if (event.eventType === "GAME_ABANDONED") {
        const abandonedBy = event.data?.["abandonedBy"]
        if (abandonedBy) {
          const playerName = this.getPlayerName(abandonedBy)
          this.gameResult = "loss"
          this.gameResultMessage = this.translate.instant("GAME.RESULT_ABANDONED")
          this.gameResultSubtitle = this.translate.instant("GAME.RESULT_ABANDONED_BY", { player: playerName })
        }
        this.snackBar.open(this.translate.instant("GAME.GAME_ABANDONED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      } else if (event.eventType === "GAME_ACTION") {
        if (this.gameId) {
          setTimeout(() => this.cardGameService.forceRefreshGame(this.gameId!), 300)
        }
      }

      this.changeDetectorRef.detectChanges()
    })

    this.cardGameService.connectToGame(this.gameId)

    this.cardGameService.getGame(this.gameId).subscribe({
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          error.error?.message || this.translate.instant("GAME.FAILED_LOAD"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.router.navigate(["/lobby"])
      },
    })
  }

  determineGameResult(): void {
    if (!this.game || !this.currentPlayer) return

    const abandonedUsers = this.game.abandonedUsers || []
    if (abandonedUsers.length > 0) {
      const currentUserId = this.currentPlayer.id
      if (abandonedUsers.includes(currentUserId)) {
        this.gameResult = "loss"
        this.gameResultMessage = this.translate.instant("GAME.RESULT_YOU_ABANDONED")
        this.gameResultSubtitle = this.translate.instant("GAME.RESULT_ABANDONED_SUBTITLE")
        return
      }

      const abandonerName = this.getPlayerName(abandonedUsers[0])
      this.gameResult = "win"
      this.gameResultMessage = this.translate.instant("GAME.RESULT_WIN")
      this.gameResultSubtitle = this.translate.instant("GAME.RESULT_OPPONENT_ABANDONED", { player: abandonerName })
      return
    }

    const scores: { [playerId: string]: number } = {}
    let highestScore = 0
    let highestScorePlayers: string[] = []

    this.game.players.forEach((player) => {
      const score = player.score || 0
      scores[player.id] = score

      if (score > highestScore) {
        highestScore = score
        highestScorePlayers = [player.id]
      } else if (score === highestScore) {
        highestScorePlayers.push(player.id)
      }
    })

    if (highestScorePlayers.length > 1) {
      this.gameResult = "draw"
      this.gameResultMessage = this.translate.instant("GAME.RESULT_DRAW")
      this.gameResultSubtitle = this.translate.instant("GAME.RESULT_DRAW_SUBTITLE", { score: highestScore })
      return
    }

    const winnerId = highestScorePlayers[0]
    const winnerName = this.getPlayerName(winnerId)
    const currentUserId = this.currentPlayer.id

    if (winnerId === currentUserId) {
      this.gameResult = "win"
      this.gameResultMessage = this.translate.instant("GAME.RESULT_WIN")
      this.gameResultSubtitle = this.translate.instant("GAME.RESULT_WIN_SUBTITLE", {
        score: highestScore,
        points: this.translate.instant("GAME.POINTS"),
      })
    } else {
      this.gameResult = "loss"
      this.gameResultMessage = this.translate.instant("GAME.RESULT_LOSS")
      this.gameResultSubtitle = this.translate.instant("GAME.RESULT_LOSS_SUBTITLE", {
        player: winnerName,
        score: highestScore,
        points: this.translate.instant("GAME.POINTS"),
      })
    }
  }

  private showTrickCompletedNotification(): void {
    if (this.lastPlayedCard) {
      const winnerName = this.getPlayerName(this.game?.currentPlayer?.id || "")
      this.snackBar.open(`${winnerName} won the trick!`, this.translate.instant("COMMON.CLOSE"), { duration: 2000 })
    }
  }

  onCardSelect(card: Card): void {
    if (!this.isCurrentPlayerTurn()) {
      this.snackBar.open(this.translate.instant("GAME.NOT_YOUR_TURN"), this.translate.instant("COMMON.CLOSE"), {
        duration: 2000,
      })
      return
    }

    this.selectedCard = card
  }

  onCardPlay(): void {
    if (!this.gameId || !this.selectedCard) return

    this.cardGameService.playCard(this.gameId, this.selectedCard)
    this.selectedCard = null

    this.lastActionTime = Date.now()
  }

  onPass(): void {
    if (!this.gameId || !this.canHit) return

    this.cardGameService.pass(this.gameId)

    this.lastActionTime = Date.now()
  }

  abandonGame(): void {
    if (!this.gameId) return

    this.snackBar.open(this.translate.instant("GAME.ABANDONING"), this.translate.instant("COMMON.CLOSE"), {
      duration: 2000,
    })

    this.cardGameService.abandonGame(this.gameId)
  }

  isCurrentPlayerTurn(): boolean {
    return this.cardGameService.isCurrentPlayerTurn()
  }

  getPlayerName(playerId: string): string {
    if (!this.game) return playerId

    const player = this.game.players.find((p) => p.id === playerId)
    return player ? player.username : playerId
  }

  getCurrentTrickCards(): Card[] {
    if (!this.game || !this.game.gameState) {
      return []
    }

    const currentTrick = this.game.gameState["currentTrick"]
    if (!currentTrick || !Array.isArray(currentTrick)) {
      return []
    }

    return currentTrick.filter((card) => card && typeof card === "object" && card.suit && card.rank)
  }

  refreshGameState(): void {
    if (this.gameId) {
      this.cardGameService.forceRefreshGame(this.gameId)
    }
  }

  goToLobby() {
    const userId = this.authService.currentUser?.id || 0
    this.lobbyService.getLobbyByPlayer(userId).subscribe((resp) => {
      if (resp === null) {
        this.router.navigate(["/lobby"])
      } else {
        this.router.navigate(["/lobby/" + resp.id])
      }
    })
  }

  protected readonly GameStatus = GameStatus
  protected readonly lobbyStatus = LOBBY_STATUS
}
