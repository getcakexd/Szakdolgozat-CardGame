import { Component, OnInit, OnDestroy } from "@angular/core"
import {ActivatedRoute, Router, RouterLink} from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Subscription } from "rxjs"
import { CardGameService } from "../../services/card-game/card-game.service"
import { AuthService } from "../../services/auth/auth.service"
import { TranslateService } from "@ngx-translate/core"
import {
  type CardGame,
  GameStatus,
  type Card,
  type Player,
  type GameEvent,
  PARTNER_MESSAGE_TYPES,
  CardSuit,
  CardRank,
} from "../../models/card-game.model"
import { CommonModule } from "@angular/common"
import { MatButtonModule } from "@angular/material/button"
import { MatCardModule } from "@angular/material/card"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { MatTooltipModule } from "@angular/material/tooltip"
import { TranslateModule } from "@ngx-translate/core"
import { CardComponent } from "../../components/card/card.component"
import { PlayerInfoComponent } from "../../components/player-info/player-info.component"
import { GameControlsComponent } from "../../components/game-controls/game-controls.component"
import {WebSocketService} from '../../services/websocket/websocket.service';

@Component({
  selector: "app-game",
  templateUrl: "./game.component.html",
  styleUrls: ["./game.component.css"],
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
    GameControlsComponent,
    RouterLink,
  ],
})
export class GameComponent implements OnInit, OnDestroy {
  game: CardGame | null = null
  currentPlayer: Player | null = null
  isLoading = true
  gameStatus = GameStatus
  selectedCard: Card | null = null
  gameEvents: GameEvent[] = []
  partnerMessages: string[] = []

  private gameId: string | null = null
  private gameSubscription: Subscription | null = null
  private eventsSubscription: Subscription | null = null

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cardGameService: CardGameService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
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

    if (!this.webSocketService.isConnected()) {
      console.log("WebSocket not connected. Attempting to reconnect...")
      this.webSocketService.reconnect()

      this.snackBar.open(this.translate.instant("GAME.CONNECTING"), this.translate.instant("COMMON.CLOSE"), {
        duration: 5000,
      })

      const connectionSub = this.webSocketService.connected$.subscribe((connected) => {
        if (connected) {
          connectionSub.unsubscribe()
          this.loadGame()
        }
      })

      setTimeout(() => {
        if (!this.webSocketService.isConnected()) {
          connectionSub.unsubscribe()
          this.snackBar.open(this.translate.instant("GAME.CONNECTION_FAILED"), this.translate.instant("COMMON.CLOSE"), {
            duration: 3000,
          })
          this.router.navigate(["/lobby"])
        }
      }, 10000)
    } else {
      this.loadGame()
    }
  }

  ngOnDestroy(): void {
    if (this.gameId) {
      this.cardGameService.disconnectFromGame(this.gameId)
    }

    if (this.gameSubscription) {
      this.gameSubscription.unsubscribe()
    }

    if (this.eventsSubscription) {
      this.eventsSubscription.unsubscribe()
    }
  }

  loadGame(): void {
    if (!this.gameId) return

    this.isLoading = true

    this.gameSubscription = this.cardGameService.currentGame$.subscribe((game) => {
      this.game = game
      if (game) {
        const userId = this.authService.currentUser?.id.toString()
        this.currentPlayer = game.players.find((p) => p.id === userId) || null
      }
      this.isLoading = false
    })

    this.eventsSubscription = this.cardGameService.gameEvents$.subscribe((event) => {
      if (!event.timestamp) {
        event.timestamp = new Date()
      }

      this.gameEvents.unshift(event)

      if (this.gameEvents.length > 10) {
        this.gameEvents.pop()
      }

      if (event.type === "PARTNER_MESSAGE" && event.data && event.data["content"]) {
        this.partnerMessages.unshift(`${event.playerId}: ${event.data["content"]}`)

        if (this.partnerMessages.length > 5) {
          this.partnerMessages.pop()
        }
      }

      if (event.type === "GAME_STARTED") {
        this.snackBar.open(this.translate.instant("GAME.GAME_STARTED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      } else if (event.type === "GAME_OVER") {
        this.snackBar.open(this.translate.instant("GAME.GAME_OVER"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      }
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
  }

  onSendPartnerMessage(messageType: string): void {
    if (!this.gameId) return

    const content = PARTNER_MESSAGE_TYPES[messageType as keyof typeof PARTNER_MESSAGE_TYPES]
    if (content) {
      this.cardGameService.sendPartnerMessage(this.gameId, "PARTNER_MESSAGE", content)
    }
  }

  isCurrentPlayerTurn(): boolean {
    return this.cardGameService.isCurrentPlayerTurn()
  }

  getPlayerName(playerId: string): string {
    if (!this.game) return playerId

    const player = this.game.players.find((p) => p.id === playerId)
    return player ? player.username : playerId
  }

  getCardImagePath(card: Card): string {
    const suit = card.suit.toLowerCase()
    const rank = card.rank.toLowerCase()
    return `/assets/cards/${suit}_${rank}.png`
  }

  getCardSuitIcon(suit: CardSuit): string {
    switch (suit) {
      case CardSuit.HEARTS:
        return "favorite"
      case CardSuit.DIAMONDS:
        return "diamond"
      case CardSuit.CLUBS:
        return "spa"
      case CardSuit.SPADES:
        return "filter_vintage"
      default:
        return "help"
    }
  }

  getCardSuitColor(suit: CardSuit): string {
    switch (suit) {
      case CardSuit.HEARTS:
      case CardSuit.DIAMONDS:
        return "red"
      case CardSuit.CLUBS:
      case CardSuit.SPADES:
        return "black"
      default:
        return "black"
    }
  }

  getCardRankDisplay(rank: CardRank): string {
    switch (rank) {
      case CardRank.SEVEN:
        return "7"
      case CardRank.EIGHT:
        return "8"
      case CardRank.NINE:
        return "9"
      case CardRank.TEN:
        return "10"
      case CardRank.UNDER:
        return "J"
      case CardRank.OVER:
        return "Q"
      case CardRank.KING:
        return "K"
      case CardRank.ACE:
        return "A"
      default:
        return "?"
    }
  }
}
