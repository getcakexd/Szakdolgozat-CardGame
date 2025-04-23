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
import { NgForOf, NgIf } from "@angular/common"
import { LobbyJoinComponent } from "../../components/lobby-join/lobby-join.component"
import { LobbyCreateComponent } from "../../components/lobby-create/lobby-create.component"

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
    NgForOf,
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
  activeLobbies: Lobby[] = []
  userLobby: Lobby | null = null
  isLoading = false
  isLoadingUserLobby = false
  selectedTabIndex = 0

  constructor(
    public lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
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
    this.loadActiveLobbies()
  }

  loadUserLobby(): void {
    if (!this.currentUser) return

    this.isLoadingUserLobby = true
    this.lobbyService.getLobbiesByPlayer(this.currentUser.id).subscribe({
      next: (lobbies) => {
        this.isLoadingUserLobby = false

        if (lobbies.length > 0) {
          this.userLobby = lobbies[0]

          this.snackBar
            .open(this.translate.instant("LOBBY.ALREADY_IN_LOBBY"), this.translate.instant("LOBBY.GO_TO_LOBBY"), {
              duration: 5000,
            })
            .onAction()
            .subscribe(() => {
              this.navigateToLobby(this.userLobby!)
            })
        } else {
          this.userLobby = null
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

  loadActiveLobbies(): void {
    this.isLoading = true
    this.activeLobbies = []
    this.isLoading = false
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

  joinLobbyByCode(code: string): void {
    if (!this.currentUser) return

    this.isLoading = true
    this.lobbyService.joinLobby(code, this.currentUser.id).subscribe({
      next: (lobby) => {
        this.isLoading = false
        this.navigateToLobby(lobby)
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.FAILED_JOIN"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
    })
  }
}
