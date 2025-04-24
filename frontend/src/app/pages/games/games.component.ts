import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Game } from '../../models/game.model';
import { LobbyService } from '../../services/lobby/lobby.service';
import { GameCardComponent } from '../../components/game-card/game-card.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-games',
  templateUrl: './games.component.html',
  styleUrls: ['./games.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    TranslateModule,
    GameCardComponent,
    MatCardModule,
    MatIconModule,
    MatButtonModule
  ]
})
export class GamesComponent implements OnInit {
  games: Game[] = [];
  isLoading = false;

  constructor(
    private lobbyService: LobbyService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.loadGames();
  }

  loadGames(): void {
    this.isLoading = true;
    this.lobbyService.getAllGames().subscribe({
      next: (games) => {
        this.games = games.filter(game => game.active);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open(
          this.translate.instant('GAMES.FAILED_LOAD'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }
}
