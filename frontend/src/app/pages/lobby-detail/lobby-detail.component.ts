import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { Lobby, LOBBY_STATUS } from '../../models/lobby.model';
import { Game } from '../../models/game.model';
import { User } from '../../models/user.model';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../services/auth/auth.service';
import {LobbyService} from '../../services/lobby/lobby.service';
import {ConfirmDialogComponent} from '../../components/confirm-dialog/confirm-dialog.component';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {MatProgressBar} from '@angular/material/progress-bar';
import {
  MatCard,
  MatCardActions,
  MatCardAvatar,
  MatCardContent,
  MatCardFooter, MatCardHeader,
  MatCardSubtitle,
  MatCardTitle
} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatList, MatListItem} from '@angular/material/list';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatOption} from '@angular/material/core';
import {MatFormField, MatLabel, MatSelect} from '@angular/material/select';
import {TranslationService} from '../../services/translation/translation.service';
import {GameCardComponent} from '../../components/game-card/game-card.component';

@Component({
  selector: 'app-lobby-detail',
  templateUrl: './lobby-detail.component.html',
  standalone: true,
  imports: [
    MatProgressSpinner,
    TranslatePipe,
    NgIf,
    MatProgressBar,
    MatCardFooter,
    MatIcon,
    MatButton,
    MatCardActions,
    MatTooltip,
    MatIconButton,
    MatListItem,
    NgForOf,
    MatList,
    MatSlideToggle,
    ReactiveFormsModule,
    MatOption,
    MatSelect,
    MatLabel,
    MatFormField,
    MatCardContent,
    MatCardSubtitle,
    MatCardTitle,
    MatCardAvatar,
    MatCardHeader,
    MatCard,
    NgClass,
    GameCardComponent,
  ],
  styleUrls: ['./lobby-detail.component.css']
})
export class LobbyDetailComponent implements OnInit, OnDestroy {
  lobby: Lobby | null = null
  currentUser: User | null = null
  isLeader = false
  isLoading = false
  games: Game[] = []
  settingsForm: FormGroup
  refreshSubscription: Subscription | null = null
  lobbyStatus = LOBBY_STATUS
  selectedGame: Game | null = null

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private lobbyService: LobbyService,
    private authService: AuthService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private translate: TranslateService,
    private translationService: TranslationService,
  ) {
    this.settingsForm = this.fb.group({
      gameId: ["", Validators.required],
      playWithPoints: [false],
    })
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser
    if (!this.currentUser) {
      this.snackBar.open(this.translate.instant("ERRORS.LOGIN_REQUIRED"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
      this.router.navigate(["/login"])
      return
    }

    this.loadGames().then(() => {
      this.loadLobby()
      this.startRefreshInterval()
    })

    this.settingsForm.get("gameId")?.valueChanges.subscribe((gameId) => {
      this.selectedGame = this.games.find((game) => game.id === gameId) || null
    })
  }

  ngOnDestroy(): void {
    this.stopRefreshInterval()
  }

  loadLobby(): void {
    const lobbyId = Number(this.route.snapshot.paramMap.get("id"))
    if (!lobbyId) {
      this.snackBar.open(this.translate.instant("LOBBY.INVALID_ID"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
      this.router.navigate(["/home"])
      return
    }

    this.isLoading = true
    this.lobbyService.getLobby(lobbyId).subscribe({
      next: (lobby) => {
        this.lobby = lobby
        this.isLeader = this.currentUser?.id === lobby.leader.id

        if (this.games.length === 0) {
          this.loadGames().then(() => {
            this.updateSettingsForm()
            this.isLoading = false
          })
        } else {
          this.updateSettingsForm()
          this.isLoading = false
        }
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          error.error.message || this.translate.instant("LOBBY.FAILED_LOAD"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.router.navigate(["/home"])
      },
    })
  }

  loadGames(): Promise<void> {
    return new Promise((resolve) => {
      this.lobbyService.getAllGames().subscribe({
        next: (games) => {
          this.games = games.filter((game) => game.active)
          resolve()
        },
        error: (error) => {
          this.snackBar.open(
            this.translate.instant("LOBBY.FAILED_LOAD_GAMES"),
            this.translate.instant("COMMON.CLOSE"),
            { duration: 3000 },
          )
          resolve()
        },
      })
    })
  }

  updateSettingsForm(): void {
    if (this.lobby) {
      this.settingsForm.patchValue({
        gameId: this.lobby.game.id,
        playWithPoints: this.lobby.playWithPoints,
      })

      this.selectedGame = this.lobby.game

      if (!this.isLeader) {
        this.settingsForm.disable()
      } else {
        this.settingsForm.enable()
      }
    }
  }

  startRefreshInterval(): void {
    this.refreshSubscription = interval(5000)
      .pipe(
        switchMap(() => {
          if (this.lobby) {
            return this.lobbyService.getLobby(this.lobby.id)
          }
          throw new Error("No lobby loaded")
        }),
      )
      .subscribe({
        next: (lobby) => {
          this.lobby = lobby
          this.isLeader = this.currentUser?.id === lobby.leader.id
          this.updateSettingsForm()
        },
        error: () => {},
      })
  }

  onSaveSettings(): void {
    if (!this.lobby || !this.isLeader || this.settingsForm.invalid) {
      return
    }

    const { gameId, playWithPoints } = this.settingsForm.value

    this.isLoading = true
    this.lobbyService.updateLobbySettings(this.lobby.id, this.currentUser!.id, gameId, playWithPoints).subscribe({
      next: (lobby) => {
        this.lobby = lobby
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.SETTINGS_UPDATED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          error.error.message || this.translate.instant("LOBBY.FAILED_UPDATE_SETTINGS"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
  }

  onStartGame(): void {
    if (!this.lobby || !this.isLeader) {
      return
    }

    this.isLoading = true
    this.lobbyService.startGame(this.lobby.id, this.currentUser!.id).subscribe({
      next: (lobby) => {
        this.lobby = lobby
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.GAME_STARTED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })

        if (lobby.status === LOBBY_STATUS.IN_GAME && lobby.cardGameId) {
          this.router.navigate(["/game", lobby.cardGameId])
        }
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(
          error.error.message || this.translate.instant("LOBBY.FAILED_START_GAME"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
  }

  onKickPlayer(player: User): void {
    if (!this.lobby || !this.isLeader || player.id === this.currentUser?.id) {
      return
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant("LOBBY.KICK_PLAYER"),
        message: this.translate.instant("LOBBY.CONFIRM_KICK", { username: player.username }),
        confirmText: this.translate.instant("LOBBY.KICK"),
        cancelText: this.translate.instant("COMMON.CANCEL"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLoading = true
        this.lobbyService.kickPlayer(this.lobby!.id, this.currentUser!.id, player.id).subscribe({
          next: (lobby) => {
            this.lobby = lobby
            this.isLoading = false
            this.snackBar.open(
              this.translate.instant("LOBBY.PLAYER_KICKED", { username: player.username }),
              this.translate.instant("COMMON.CLOSE"),
              { duration: 3000 },
            )
          },
          error: (error) => {
            this.isLoading = false
            this.snackBar.open(
              error.error.message || this.translate.instant("LOBBY.FAILED_KICK_PLAYER"),
              this.translate.instant("COMMON.CLOSE"),
              { duration: 3000 },
            )
          },
        })
      }
    })
  }

  onLeaveLobby(): void {
    if (!this.lobby) {
      return
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant("LOBBY.LEAVE_LOBBY"),
        message: this.isLeader
          ? this.translate.instant("LOBBY.CONFIRM_LEAVE_LEADER")
          : this.translate.instant("LOBBY.CONFIRM_LEAVE"),
        confirmText: this.translate.instant("LOBBY.LEAVE"),
        cancelText: this.translate.instant("COMMON.CANCEL"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.isLoading = true
        this.lobbyService.leaveLobby(this.lobby!.id, this.currentUser!.id).subscribe({
          next: (response) => {
            this.isLoading = false
            this.snackBar.open(this.translate.instant("LOBBY.LEFT_LOBBY"), this.translate.instant("COMMON.CLOSE"), {
              duration: 3000,
            })
            this.router.navigate(["/home"])
          },
          error: (error) => {
            this.isLoading = false
            this.snackBar.open(
              error.error.message || this.translate.instant("LOBBY.FAILED_LEAVE"),
              this.translate.instant("COMMON.CLOSE"),
              { duration: 3000 },
            )
          },
        })
      }
    })
  }

  copyLobbyCode(): void {
    if (!this.lobby) {
      return
    }

    navigator.clipboard.writeText(this.lobby.code).then(() => {
      this.snackBar.open(this.translate.instant("LOBBY.CODE_COPIED"), this.translate.instant("COMMON.CLOSE"), {
        duration: 2000,
      })
    })
  }

  canStartGame(): boolean {
    return (
      this.isLeader &&
      this.lobby !== null &&
      this.lobby.status === LOBBY_STATUS.WAITING &&
      this.lobby.players.length >= this.lobby.minPlayers
    )
  }

  getGameById(id: number): Game | undefined {
    return this.games.find((game: Game) => game.id === id)
  }

  goToGame(): void {
    if (this.lobby && this.lobby.cardGameId) {
      this.router.navigate(["/game", this.lobby.cardGameId])
    } else {
      this.snackBar.open(this.translate.instant("LOBBY.GAME_ID_NOT_FOUND"), this.translate.instant("COMMON.CLOSE"), {
        duration: 3000,
      })
    }
  }

  private stopRefreshInterval() {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe()
      this.refreshSubscription = null
    }
  }
}
