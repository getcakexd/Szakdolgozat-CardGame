import { Component, OnInit } from "@angular/core"
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
import {TranslateModule, TranslateService} from "@ngx-translate/core"
import { StatsService } from "../../services/stats/stats.service"
import { AuthService } from "../../services/auth/auth.service"
import { LeaderboardEntry } from "../../models/user-stats.model"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Game } from '../../models/game.model';
import {IS_DEV} from '../../../environments/api-config';

@Component({
  selector: "app-leaderboard",
  templateUrl: "./leaderboard.component.html",
  styleUrls: ["./leaderboard.component.scss"],
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
    TranslateModule,
  ],
})
export class LeaderboardComponent implements OnInit {
  userId: number
  isLoading = false
  overallLeaderboard: LeaderboardEntry[] = []
  gameLeaderboard: LeaderboardEntry[] = []
  games: Game[] = []
  selectedGameId: number | null = null

  leaderboardColumns: string[] = ["rank", "username", "gamesPlayed", "gamesWon", "winRate", "points"]

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
    this.loadOverallLeaderboard()
  }

  loadGames(): void {
    this.statsService.getAllGames().subscribe({
      next: (games) => {
        this.games = games.filter((game) => game.active)
      },
      error: (error) => {
        if(IS_DEV) console.error("Error loading games:", error)
        this.snackBar.open(
          this.translate.instant("LEADERBOARD.FAILED_LOAD_GAMES"),
          this.translate.instant("COMMON.CLOSE"),
          {
            duration: 5000,
            panelClass: ["error-snackbar"],
          },
        )
      },
    })
  }

  loadOverallLeaderboard(): void {
    this.isLoading = true
    this.statsService.getLeaderboard().subscribe({
      next: (leaderboard) => {
        if (IS_DEV) console.log("Overall leaderboard:", leaderboard)
        this.overallLeaderboard = leaderboard
        this.isLoading = false
      },
      error: (error) => {
        if(IS_DEV) console.error("Error loading leaderboard:", error)
        this.snackBar.open(this.translate.instant("LEADERBOARD.FAILED_LOAD"), this.translate.instant("COMMON.CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
        this.isLoading = false
      },
    })
  }

  loadGameLeaderboard(): void {
    if (!this.selectedGameId) return

    this.isLoading = true
    this.statsService.getLeaderboard(this.selectedGameId).subscribe({
      next: (leaderboard) => {
        if (IS_DEV) console.log("Game leaderboard:", leaderboard)
        this.gameLeaderboard = leaderboard
        this.isLoading = false
      },
      error: (error) => {
        if(IS_DEV) console.error("Error loading game leaderboard:", error)
        this.snackBar.open(this.translate.instant("LEADERBOARD.FAILED_LOAD"), this.translate.instant("COMMON.CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
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
