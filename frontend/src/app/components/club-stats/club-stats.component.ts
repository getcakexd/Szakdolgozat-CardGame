import { Component, OnInit, Input } from "@angular/core"
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
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { IS_DEV } from "../../../environments/api-config"
import { Game } from "../../models/game.model"
import { GameService } from "../../services/game/game.service"
import { ClubStats, ClubGameStats, ClubMemberStats } from "../../models/club-stats.model"
import { Club } from "../../models/club.model"

@Component({
  selector: "app-club-stats",
  templateUrl: "./club-stats.component.html",
  styleUrls: ["./club-stats.component.scss"],
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
export class ClubStatsComponent implements OnInit {
  @Input() club!: Club

  userId: number
  isLoading = false
  clubStats: ClubStats | null = null
  clubGameStats: ClubGameStats[] = []
  clubMemberStats: ClubMemberStats[] = []
  games: Map<number, Game> = new Map()

  gameStatsColumns: string[] = ["gameName", "gamesPlayed", "points", "fatsCollected", "uniquePlayers", "lastPlayed"]
  memberStatsColumns: string[] = ["username", "gamesPlayed", "gamesWon", "winRate", "points", "fatsCollected"]

  constructor(
    private statsService: StatsService,
    private gameService: GameService,
    private authService: AuthService,
    private translate: TranslateService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
  }

  ngOnInit(): void {
    this.loadGames()
    if (this.club) {
      this.loadClubStats()
    }
  }

  ngOnChanges(): void {
    if (this.club) {
      this.loadClubStats()
    }
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

  loadClubStats(): void {
    this.isLoading = true

    this.statsService.getClubStats(this.club.id).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("Club stats:", stats)
        this.clubStats = stats
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error loading club stats:", error)
        this.snackBar.open(this.translate.instant("CLUB.STATS.FAILED_LOAD"), this.translate.instant("COMMON.CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
        this.isLoading = false
      },
    })

    this.statsService.getClubGameStats(this.club.id).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("Club game stats:", stats)
        this.clubGameStats = stats
      },
      error: (error) => {
        console.error("Error loading club game stats:", error)
      },
    })

    this.statsService.getClubMemberStats(this.club.id).subscribe({
      next: (stats) => {
        if (IS_DEV) console.log("Club member stats:", stats)
        this.clubMemberStats = stats
      },
      error: (error) => {
        console.error("Error loading club member stats:", error)
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

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString()
  }
}
