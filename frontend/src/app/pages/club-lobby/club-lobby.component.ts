import { Component, OnInit, Input } from "@angular/core"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { MatDialog } from "@angular/material/dialog"
import { TranslatePipe, TranslateService } from "@ngx-translate/core"
import { NgIf, NgFor } from "@angular/common"
import { MatProgressSpinner } from "@angular/material/progress-spinner"
import { MatButton } from "@angular/material/button"
import { MatIcon } from "@angular/material/icon"
import { MatTab, MatTabGroup } from "@angular/material/tabs"
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle,
} from "@angular/material/card"

import { Lobby } from "../../models/lobby.model"
import { User } from "../../models/user.model"
import { Club } from "../../models/club.model"
import { LobbyService } from "../../services/lobby/lobby.service"
import { AuthService } from "../../services/auth/auth.service"
import {ClubLobbyCreateComponent} from '../../components/club-lobby-create/club-lobby-create.component';

@Component({
  selector: "app-club-lobby",
  templateUrl: "./club-lobby.component.html",
  standalone: true,
  imports: [
    TranslatePipe,
    MatProgressSpinner,
    MatButton,
    MatCardActions,
    MatCardContent,
    MatCardSubtitle,
    MatCardTitle,
    NgIf,
    NgFor,
    MatCardHeader,
    MatCard,
    MatIcon,
    MatTab,
    MatTabGroup,
    ClubLobbyCreateComponent,
  ],
  styleUrls: ["./club-lobby.component.css"],
})
export class ClubLobbyComponent implements OnInit {
  @Input() club!: Club

  currentUser: User | null = null
  clubLobbies: Lobby[] = []
  isLoading = false
  selectedTabIndex = 0

  constructor(
    private lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser
    if (!this.currentUser) {
      this.snackBar.open(this.translate.instant("ERRORS.LOGIN_REQUIRED"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
      this.router.navigate(["/login"])
      return
    }

    this.loadClubLobbies()
  }

  loadClubLobbies(): void {
    if (!this.club) return

    this.isLoading = true
    this.lobbyService.getClubLobbies(this.club.id).subscribe({
      next: (lobbies) => {
        this.clubLobbies = lobbies
        this.isLoading = false
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          this.translate.instant("LOBBY.FAILED_LOAD_CLUB_LOBBIES"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
  }

  onTabChange(event: any): void {
    if (event && event.index !== undefined) {
      this.selectedTabIndex = event.index

      // Refresh club lobbies when switching to the club lobbies tab
      if (event.index === 0) {
        this.loadClubLobbies()
      }
    }
  }

  navigateToLobby(lobby: Lobby): void {
    this.router.navigate(["/lobby", lobby.id])
  }

  joinLobby(lobby: Lobby): void {
    if (!this.currentUser) return

    this.isLoading = true
    this.lobbyService.joinLobby(lobby.code, this.currentUser.id).subscribe({
      next: (lobby) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.JOIN.SUCCESS"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.router.navigate(["/lobby", lobby.id])
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          error.error.message || this.translate.instant("LOBBY.JOIN.FAILED"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
  }

  refreshLobbies(): void {
    this.loadClubLobbies()
  }
}
