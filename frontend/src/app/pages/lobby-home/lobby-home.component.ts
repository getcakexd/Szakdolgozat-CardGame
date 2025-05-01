import { Component, OnInit, ViewChild } from "@angular/core"
import { Router } from "@angular/router"
import { MatSnackBar } from "@angular/material/snack-bar"
import { MatTab, MatTabGroup } from "@angular/material/tabs"
import { User } from "../../models/user.model"
import { Lobby } from "../../models/lobby.model"
import { LobbyService } from "../../services/lobby/lobby.service"
import { AuthService } from "../../services/auth/auth.service"
import { TranslatePipe, TranslateService } from "@ngx-translate/core"
import { MatProgressSpinner } from "@angular/material/progress-spinner"
import { MatButton } from "@angular/material/button"
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle,
} from "@angular/material/card"
import { NgIf } from "@angular/common"
import { LobbyJoinComponent } from "../../components/lobby-join/lobby-join.component"
import { LobbyCreateComponent } from "../../components/lobby-create/lobby-create.component"
import {ConfirmDialogComponent} from '../../components/confirm-dialog/confirm-dialog.component';
import {CardGameService} from '../../services/card-game/card-game.service';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: "app-lobby-home",
  templateUrl: "./lobby-home.component.html",
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
    MatCardHeader,
    MatCard,
    LobbyJoinComponent,
    MatTab,
    LobbyCreateComponent,
    MatTabGroup,
  ],
  styleUrls: ["./lobby-home.component.css"],
})
export class LobbyHomeComponent implements OnInit {
  @ViewChild("lobbyTabs") lobbyTabs!: MatTabGroup

  currentUser: User | null = null
  userLobby: Lobby | null = null
  isLoading = false
  isLoadingUserLobby = false
  selectedTabIndex = 0

  constructor(
    public lobbyService: LobbyService,
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

    const savedTabIndex = localStorage.getItem("lobbyTabIndex")
    if (savedTabIndex) {
      this.selectedTabIndex = Number.parseInt(savedTabIndex, 10)
    }

    this.loadUserLobby()
  }

  loadUserLobby(): void {
    if (!this.currentUser) return

    this.isLoadingUserLobby = true
    this.lobbyService.getLobbyByPlayer(this.currentUser.id).subscribe({
      next: (lobby) => {
        this.isLoadingUserLobby = false

        if (lobby !== null) {
          this.userLobby = lobby

          this.snackBar
            .open(this.translate.instant("LOBBY.ALREADY_IN_LOBBY"), this.translate.instant("LOBBY.GO_TO_LOBBY"), {
              duration: 5000,
            })
            .onAction()
            .subscribe(() => {
              this.navigateToLobby(this.userLobby!)
            })
        }
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

  onTabChange(event: any): void {
    if (event && event.index !== undefined) {
      this.selectedTabIndex = event.index
      localStorage.setItem("lobbyTabIndex", event.index.toString())
    } else if (typeof event === "number") {
      this.selectedTabIndex = event
      localStorage.setItem("lobbyTabIndex", event.toString())
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
        this.loadUserLobby()
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
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("GAME.FAILED_ABANDON"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
    })
  }
}
