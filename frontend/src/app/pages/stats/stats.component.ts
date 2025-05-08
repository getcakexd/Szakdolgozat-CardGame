import { Component, OnInit } from "@angular/core"
import { MatCardModule } from "@angular/material/card"
import { MatTabsModule } from "@angular/material/tabs"
import { MatTableModule } from "@angular/material/table"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatDividerModule } from "@angular/material/divider"
import { MatButtonModule } from "@angular/material/button"
import { MatChipsModule } from "@angular/material/chips"
import { MatTooltipModule } from "@angular/material/tooltip"
import { NgIf, NgClass, DatePipe, DecimalPipe } from "@angular/common"
import { TranslatePipe, TranslateService } from "@ngx-translate/core"
import { StatsService } from "../../services/stats/stats.service"
import { AuthService } from "../../services/auth/auth.service"
import { UserStats, UserGameStats, GameStatistics } from "../../models/user-stats.model"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { IS_DEV } from "../../../environments/api-config"
import { Game } from "../../models/game.model"
import { GameService } from "../../services/game/game.service"

@Component({
  selector: "app-stats",
  templateUrl: "./stats.component.html",
  styleUrls: ["./stats.component.scss"],
  standalone: true,
  imports: [
    NgIf,
    NgClass,
    DatePipe,
    DecimalPipe,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatIconModule,
    MatProgressBarModule,
    MatDividerModule,
    MatButtonModule,
    MatChipsModule,
    MatTooltipModule,
    TranslatePipe,
  ],
})
export class StatsComponent implements OnInit {
  userId: number
  isLoading = false
  userStats: UserStats | null = null
  userGameStats: UserGameStats[] = []
  recentGames: GameStatistics[] = []
  games: Map<number, Game> = new Map()
  drawStats: any = null

  gameStatsColumns: string[] = [
    "gameName",
    "gamesPlayed",
    "gamesWon",
    "gamesDrawn",
    "winRate",
    "points",
    "fatsCollected",
    "streak",
  ]
  recentGamesColumns: string[] = ["gameType", "result", "score", "fatsCollected", "playedAt", "isFriendly"]

  constructor(
    private statsService: StatsService,
    private gameService: GameService,
    private authService: AuthService,
    private translate: TranslateService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(["/login"]).then()
    }
  }

  ngOnInit(): void {
    this.loadGames()
    this.loadStats()
  }

  loadGames(): void {
    this.gameService.getActiveGames().subscribe({
      next: (games: Game[]) => {
        games.forEach((game) => {
          this.games.set(game.id, game)
        })
        if (IS_DEV) console.log("Loaded games:", this.games)
      },
      error: (error: any) => {
        console.error("Error loading games:", error)
      },
    })
  }

  loadStats(): void {
    this.isLoading = true

    this.statsService.getUserStats(this.userId).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("User stats:", stats)
        this.userStats = stats
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error loading user stats:", error)
        this.snackBar.open(this.translate.instant("STATS.FAILED_LOAD"), this.translate.instant("COMMON.CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
        this.isLoading = false
      },
    })

    this.statsService.getUserDrawStats(this.userId).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("User draw stats:", stats)
        this.drawStats = stats
      },
      error: (error) => {
        console.error("Error loading draw stats:", error)
      },
    })

    this.statsService.getUserGameStats(this.userId).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("User game stats:", stats)
        this.userGameStats = stats
      },
      error: (error) => {
        console.error("Error loading game stats:", error)
      },
    })

    this.statsService.getRecentGames(this.userId, 10).subscribe({
      next: (games) => {
        this.recentGames = games
        if (IS_DEV) console.log("Recent games:", games)
      },
      error: (error) => {
        console.error("Error loading recent games:", error)
      },
    })
  }

  getGameName(gameId: number): string {
    const game = this.games.get(gameId)
    return game ? game.name : this.translate.instant("STATS.UNKNOWN_GAME")
  }

  calculateWinRate(won: number, played: number): number {
    if (played === 0) return 0
    return (won / played) * 100
  }

  calculateDrawRate(drawn: number, played: number): number {
    if (played === 0) return 0
    return (drawn / played) * 100
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString()
  }

  getGameResult(game: GameStatistics): string {
    console.log("Game result:", game)
    if (game.won) return this.translate.instant("STATS.WON")
    if (game.drawn) return this.translate.instant("STATS.DRAWN")
    return this.translate.instant("STATS.LOST")
  }

  getResultClass(game: GameStatistics): string {
    if (game.won) return "win"
    if (game.drawn) return "draw"
    return "loss"
  }

  viewGame(gameId: string): void {
    this.router.navigate(["/game", gameId])
  }

  viewLeaderboard(): void {
    this.router.navigate(["/leaderboard"])
  }
}
