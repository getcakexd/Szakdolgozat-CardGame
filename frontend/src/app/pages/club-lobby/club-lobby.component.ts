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
import {ConfirmDialogComponent} from '../../components/confirm-dialog/confirm-dialog.component';
import {CardGameService} from '../../services/card-game/card-game.service';

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
  userLobby: Lobby | null = null
  isLoading = false
  isLoadingUserLobby = false
  selectedTabIndex = 0
  lobbyStatus = {
    WAITING: "WAITING",
    IN_GAME: "IN_GAME",
    FINISHED: "FINISHED",
  }

  constructor(
    private lobbyService: LobbyService,
    private authService: AuthService,
    private cardGameService: CardGameService,
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

    this.loadUserLobby()
    this.loadClubLobbies()
  }

  loadUserLobby(): void {
    if (!this.currentUser) return

    this.isLoadingUserLobby = true
    this.lobbyService.getLobbyByPlayer(this.currentUser.id).subscribe({
      next: (lobby) => {
        this.isLoadingUserLobby = false
        this.userLobby = lobby
      },
      error: (error) => {
        this.isLoadingUserLobby = false
        this.snackBar.open(
          this.translate.instant("LOBBY.FAILED_CHECK_LOBBIES"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
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

      if (event.index === 0) {
        this.loadClubLobbies()
      }
    }
  }

  navigateToLobby(lobby: Lobby): void {
    this.router.navigate(["/lobby", lobby.id])
  }

  navigateToGame(lobby: Lobby): void {
    if (lobby.cardGameId) {
      this.router.navigate(["/game", lobby.cardGameId])
    } else {
      this.snackBar.open(this.translate.instant("GAME.INVALID_ID"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
    }
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
        this.loadUserLobby()
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

  confirmLeaveLobby(lobby: Lobby): void {
    if (!this.currentUser) return

    const isLeader = lobby.leader.id === this.currentUser.id

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "350px",
      data: {
        title: this.translate.instant("LOBBY.LEAVE_LOBBY"),
        message: isLeader
          ? this.translate.instant("LOBBY.CONFIRM_LEAVE_LEADER")
          : this.translate.instant("LOBBY.CONFIRM_LEAVE"),
        confirmText: this.translate.instant("LOBBY.LEAVE"),
        cancelText: this.translate.instant("COMMON.CLOSE"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.leaveLobby(lobby)
      }
    })
  }

  leaveLobby(lobby: Lobby): void {
    if (!this.currentUser) return

    this.isLoading = true
    this.lobbyService.leaveLobby(lobby.id, this.currentUser.id).subscribe({
      next: (response) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.LEFT_LOBBY"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.userLobby = null
        this.loadClubLobbies()
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.FAILED_LEAVE"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
    })
  }

  confirmAbandonGame(lobby: Lobby): void {
    if (!this.currentUser || !lobby.cardGameId) return

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "350px",
      data: {
        title: this.translate.instant("GAME.ABANDON_GAME"),
        message: this.translate.instant("GAME.CONFIRM_ABANDON"),
        confirmText: this.translate.instant("GAME.ABANDON"),
        cancelText: this.translate.instant("COMMON.CLOSE"),
        confirmColor: "warn",
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.abandonGame(lobby)
      }
    })
  }

  abandonGame(lobby: Lobby): void {
    if (!this.currentUser || !lobby.cardGameId) return

    this.isLoading = true
    this.cardGameService.abandonGame(lobby.cardGameId).subscribe({
      next: () => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("GAME.GAME_ABANDONED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.loadUserLobby()
        this.loadClubLobbies()
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("GAME.FAILED_ABANDON"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
    })
  }

  refreshLobbies(): void {
    this.loadUserLobby()
    this.loadClubLobbies()
  }

  isUserInLobby(): boolean {
    return this.userLobby !== null
  }

  isUserInClubLobby(): boolean {
    if (!this.userLobby || !this.club) return false
    return this.userLobby.club?.id === this.club.id
  }
}
