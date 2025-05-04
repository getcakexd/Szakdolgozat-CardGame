import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Game } from '../../models/game.model';
import { User } from '../../models/user.model';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {AuthService} from '../../services/auth/auth.service';
import {LobbyService} from '../../services/lobby/lobby.service';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {NgForOf, NgIf} from '@angular/common';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {GameCardComponent} from '../game-card/game-card.component';
import {TranslationService} from '../../services/translation/translation.service';

@Component({
  selector: 'app-lobby-create',
  templateUrl: './lobby-create.component.html',
  standalone: true,
  imports: [
    MatProgressSpinner,
    TranslatePipe,
    MatIcon,
    MatButton,
    NgIf,
    ReactiveFormsModule,
    MatSlideToggle,
    MatError,
    MatOption,
    MatSelect,
    NgForOf,
    MatLabel,
    MatFormField,
    GameCardComponent
  ],
  styleUrls: ['./lobby-create.component.scss']
})
export class LobbyCreateComponent implements OnInit {
  createForm: FormGroup
  games: Game[] = []
  currentUser: User | null = null
  isLoading = false
  isSubmitting = false
  selectedGame: Game | null = null

  constructor(
    private fb: FormBuilder,
    private lobbyService: LobbyService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private translationService: TranslationService,
  ) {
    this.createForm = this.fb.group({
      gameId: ["", Validators.required],
      playWithPoints: [false],
      isPublic: [false],
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

    this.loadGames()

    this.createForm.get("gameId")?.valueChanges.subscribe((gameId) => {
      this.selectedGame = this.games.find((game) => game.id === gameId) || null
    })
  }

  loadGames(): void {
    this.isLoading = true
    this.lobbyService.getAllGames().subscribe({
      next: (games) => {
        this.games = games.filter((game) => game.active)
        this.isLoading = false
      },
      error: (error) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("LOBBY.FAILED_LOAD_GAMES"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
      },
    })
  }

  onSubmit(): void {
    if (this.createForm.invalid || !this.currentUser) {
      return
    }

    const { gameId, playWithPoints, isPublic } = this.createForm.value

    this.isSubmitting = true
    this.lobbyService.createLobby(this.currentUser.id, gameId, playWithPoints, isPublic).subscribe({
      next: (lobby) => {
        this.isSubmitting = false
        this.snackBar.open(this.translate.instant("LOBBY.CREATE.SUCCESS"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.router.navigate(["/lobby", lobby.id])
      },
      error: (error) => {
        this.isSubmitting = false
        this.snackBar.open(
          error.error.message || this.translate.instant("LOBBY.CREATE.FAILED"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
      },
    })
  }

  getSelectedGame(): Game | null {
    const gameId = this.createForm.get("gameId")?.value
    return this.games.find((game) => game.id === gameId) || null
  }
}
