import { Component, OnInit, Input } from "@angular/core"
import { MatCardModule } from "@angular/material/card"
import { MatTabsModule } from "@angular/material/tabs"
import { MatTableModule } from "@angular/material/table"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatDividerModule } from "@angular/material/divider"
import { MatButtonModule } from "@angular/material/button"
import { MatSelectModule } from "@angular/material/select"
import { MatFormFieldModule } from "@angular/material/form-field"
import { NgIf, NgFor, NgClass, DecimalPipe } from "@angular/common"
import { FormsModule } from "@angular/forms"
import { TranslatePipe, TranslateService } from "@ngx-translate/core"
import { StatsService } from "../../services/stats/stats.service"
import { AuthService } from "../../services/auth/auth.service"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Game } from "../../models/game.model"
import { IS_DEV } from "../../../environments/api-config"
import { Club } from "../../models/club.model"
import { ClubMemberStats } from "../../models/club-stats.model"

@Component({
  selector: "app-club-leaderboard",
  templateUrl: "./club-leaderboard.component.html",
  styleUrls: ["./club-leaderboard.component.scss"],
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    NgClass,
    DecimalPipe,
    FormsModule,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatIconModule,
    MatProgressBarModule,
    MatDividerModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    TranslatePipe,
  ],
})
export class ClubLeaderboardComponent implements OnInit {
  @Input() club!: Club

  userId: number
  isLoading = false
  memberLeaderboard: ClubMemberStats[] = []
  games: Game[] = []
  selectedGameId: number | null = null
  gameLeaderboard: ClubMemberStats[] = []

  leaderboardColumns: string[] = ["rank", "username", "gamesPlayed", "gamesWon", "winRate", "points", "fatsCollected"]

  constructor(
    private statsService: StatsService,
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
      this.loadMemberLeaderboard()
    }
  }

  ngOnChanges(): void {
    if (this.club) {
      this.loadMemberLeaderboard()
    }
  }

  loadGames(): void {
    this.statsService.getAllGames().subscribe({
      next: (games) => {
        this.games = games.filter((game) => game.active)
      },
      error: (error) => {
        console.error("Error loading games:", error)
        this.snackBar.open(
          this.translate.instant("CLUB.LEADERBOARD.FAILED_LOAD_GAMES"),
          this.translate.instant("COMMON.CLOSE"),
          {
            duration: 5000,
            panelClass: ["error-snackbar"],
          },
        )
      },
    })
  }

  loadMemberLeaderboard(): void {
    this.isLoading = true
    this.statsService.getClubMemberLeaderboard(this.club.id).subscribe({
      next: (leaderboard) => {
        if (IS_DEV) console.log("Club member leaderboard:", leaderboard)
        this.memberLeaderboard = leaderboard
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error loading club member leaderboard:", error)
        this.snackBar.open(
          this.translate.instant("CLUB.LEADERBOARD.FAILED_LOAD"),
          this.translate.instant("COMMON.CLOSE"),
          {
            duration: 5000,
            panelClass: ["error-snackbar"],
          },
        )
        this.isLoading = false
      },
    })
  }

  loadGameLeaderboard(): void {
    if (!this.selectedGameId) return

    this.isLoading = true
    this.statsService.getClubMemberLeaderboardByGame(this.club.id, this.selectedGameId).subscribe({
      next: (leaderboard) => {
        if (IS_DEV) console.log("Club game leaderboard:", leaderboard)
        this.gameLeaderboard = leaderboard
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error loading club game leaderboard:", error)
        this.snackBar.open(
          this.translate.instant("CLUB.LEADERBOARD.FAILED_LOAD"),
          this.translate.instant("COMMON.CLOSE"),
          {
            duration: 5000,
            panelClass: ["error-snackbar"],
          },
        )
        this.gameLeaderboard = []
        this.isLoading = false
      },
    })
  }

  onGameChange(): void {
    this.loadGameLeaderboard()
  }

  calculateWinRate(won: number, played: number): number {
    if (played === 0) return 0
    return (won / played) * 100
  }
}
