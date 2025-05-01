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
import {TranslatePipe, TranslateService} from "@ngx-translate/core"
import { StatsService } from "../../services/stats/stats.service"
import { AuthService } from "../../services/auth/auth.service"
import { UserStats, UserGameStats, GameStatistics } from "../../models/user-stats.model"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"

@Component({
  selector: "app-stats",
  templateUrl: "./stats.component.html",
  styleUrls: ["./stats.component.css"],
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

  gameStatsColumns: string[] = ["gameName", "gamesPlayed", "gamesWon", "winRate", "points", "fatsCollected", "streak"]
  recentGamesColumns: string[] = ["gameType", "result", "score", "fatsCollected", "playedAt"]

  constructor(
    private statsService: StatsService,
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
    this.loadStats()
  }

  loadStats(): void {
    this.isLoading = true

    this.statsService.getUserStats(this.userId).subscribe({
      next: (stats) => {
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

    this.statsService.getUserGameStats(this.userId).subscribe({
      next: (stats) => {
        this.userGameStats = stats
      },
      error: (error) => {
        console.error("Error loading game stats:", error)
      },
    })

    this.statsService.getRecentGames(this.userId, 10).subscribe({
      next: (games) => {
        this.recentGames = games
      },
      error: (error) => {
        console.error("Error loading recent games:", error)
      },
    })
  }

  calculateWinRate(won: number, played: number): number {
    if (played === 0) return 0
    return (won / played) * 100
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString()
  }

  viewGame(gameId: string): void {
    this.router.navigate(["/game", gameId])
  }

  viewLeaderboard(): void {
    this.router.navigate(["/leaderboard"])
  }
}
