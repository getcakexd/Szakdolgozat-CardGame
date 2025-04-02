import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {Lobby} from '../../models/lobby.model';
import {LobbyMessage} from '../../models/LobbyMessage.model';
import {UserService} from '../../services/user/user.service';
import {LobbyService} from '../../services/lobby/lobby.service';
import {LobbyCreateDialogComponent} from '../../components/lobby-create-dialog/lobby-create-dialog.component';
import {LobbyJoinDialogComponent} from '../../components/lobby-join-dialog/lobby-join-dialog.component';
import {ConfirmDialogComponent} from '../profile/profile.component';
import {LobbyPlayer} from '../../models/LobbyPlayer.model';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {AsyncPipe, DatePipe, NgForOf, NgIf} from '@angular/common';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatList, MatListItem} from '@angular/material/list';
import {CdkCopyToClipboard} from '@angular/cdk/clipboard';
import {MatTooltip} from '@angular/material/tooltip';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {FormsModule} from '@angular/forms';
import {MatDivider} from '@angular/material/divider';


@Component({
  selector: 'app-lobby-page',
  templateUrl: './lobby-page.component.html',
  standalone: true,
  imports: [
    MatProgressSpinner,
    MatCardTitle,
    MatCardHeader,
    MatCard,
    AsyncPipe,
    NgIf,
    MatIcon,
    MatButton,
    MatCardContent,
    MatListItem,
    MatList,
    MatIconButton,
    CdkCopyToClipboard,
    MatTooltip,
    MatDivider,
    DatePipe,
    NgForOf,
    MatLabel,
    FormsModule,
    MatFormField
  ],
  styleUrls: ['./lobby-page.component.scss']
})
export class LobbyPageComponent implements OnInit, OnDestroy {
  currentLobby$: Observable<Lobby | null>;
  lobbyMessages$: Observable<LobbyMessage[]>;
  publicLobbies: Lobby[] = [];

  messageContent: string = '';
  isLoading: boolean = false;

  private subscriptions: Subscription[] = [];

  constructor(
    private lobbyService: LobbyService,
    private userService: UserService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.currentLobby$ = this.lobbyService.currentLobby$;
    this.lobbyMessages$ = this.lobbyService.lobbyMessages$;
  }

  ngOnInit(): void {
    this.loadPublicLobbies();

    if (!this.userService.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadPublicLobbies(): void {
    this.isLoading = true;
    this.subscriptions.push(
      this.lobbyService.getPublicLobbies().subscribe({
        next: (lobbies) => {
          this.publicLobbies = lobbies;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading lobbies', error);
          this.snackBar.open('Failed to load lobbies', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      })
    );
  }

  openCreateLobbyDialog(): void {
    const dialogRef = this.dialog.open(LobbyCreateDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.createLobby(result);
      }
    });
  }

  createLobby(lobbyData: Partial<Lobby>): void {
    this.isLoading = true;
    this.subscriptions.push(
      this.lobbyService.createLobby(lobbyData).subscribe({
        next: () => {
          this.isLoading = false;
          this.snackBar.open('Lobby created successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error creating lobby', error);
          this.snackBar.open(error.error?.message || 'Failed to create lobby', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      })
    );
  }

  openJoinLobbyDialog(): void {
    const dialogRef = this.dialog.open(LobbyJoinDialogComponent, {
      width: '400px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.joinLobbyByCode(result);
      }
    });
  }

  joinLobbyByCode(code: string): void {
    this.isLoading = true;
    this.subscriptions.push(
      this.lobbyService.joinLobbyByCode(code).subscribe({
        next: () => {
          this.isLoading = false;
          this.snackBar.open('Joined lobby successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error joining lobby', error);
          this.snackBar.open(error.error?.message || 'Failed to join lobby', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      })
    );
  }

  joinPublicLobby(lobby: Lobby): void {
    this.joinLobbyByCode(lobby.code);
  }

  leaveLobby(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {
        title: 'Leave Lobby',
        message: 'Are you sure you want to leave this lobby?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.isLoading = true;
        this.subscriptions.push(
          this.lobbyService.leaveLobby().subscribe({
            next: () => {
              this.isLoading = false;
              this.snackBar.open('Left lobby successfully', 'Close', { duration: 3000 });
              this.loadPublicLobbies();
            },
            error: (error) => {
              console.error('Error leaving lobby', error);
              this.snackBar.open('Failed to leave lobby', 'Close', { duration: 3000 });
              this.isLoading = false;
            }
          })
        );
      }
    });
  }

  kickPlayer(player: LobbyPlayer): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {
        title: 'Kick Player',
        message: `Are you sure you want to kick ${player.user.username} from the lobby?`
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.isLoading = true;
        this.subscriptions.push(
          this.lobbyService.kickPlayer(player.user.id).subscribe({
            next: () => {
              this.isLoading = false;
              this.snackBar.open(`Kicked ${player.user.username} from the lobby`, 'Close', { duration: 3000 });
            },
            error: (error) => {
              console.error('Error kicking player', error);
              this.snackBar.open('Failed to kick player', 'Close', { duration: 3000 });
              this.isLoading = false;
            }
          })
        );
      }
    });
  }

  toggleReady(): void {
    this.subscriptions.push(
      this.lobbyService.toggleReady().subscribe({
        error: (error) => {
          console.error('Error toggling ready status', error);
          this.snackBar.open('Failed to toggle ready status', 'Close', { duration: 3000 });
        }
      })
    );
  }

  sendMessage(): void {
    if (!this.messageContent.trim()) return;

    this.subscriptions.push(
      this.lobbyService.sendMessage(this.messageContent).subscribe({
        next: () => {
          this.messageContent = '';
        },
        error: (error) => {
          console.error('Error sending message', error);
          this.snackBar.open('Failed to send message', 'Close', { duration: 3000 });
        }
      })
    );
  }

  isLobbyLeader(): boolean {
    return this.lobbyService.isUserLobbyLeader();
  }

  getCurrentPlayer(): LobbyPlayer | undefined | null {
    return this.lobbyService.getCurrentUserInLobby();
  }

  areAllPlayersReady(): boolean {
    return this.lobbyService.areAllPlayersReady();
  }

  startGame(): void {
    this.snackBar.open('Game starting...', 'Close', { duration: 3000 });
  }

  toggleLobbyVisibility(lobby: Lobby): void {
    const updateData: Partial<Lobby> = {
      ...lobby,
      isPublic: !lobby.isPublic
    };

    this.updateLobbySettings(updateData);
  }

  togglePointsSettings(lobby: Lobby): void {
    const updateData: Partial<Lobby> = {
      ...lobby,
      withPoints: !lobby.withPoints
    };

    this.updateLobbySettings(updateData);
  }

  changeGameMode(lobby: Lobby): void {
    const gameModes = ['classic', 'advanced'];
    const currentIndex = gameModes.indexOf(lobby.gameMode);
    const nextIndex = (currentIndex + 1) % gameModes.length;

    const updateData: Partial<Lobby> = {
      ...lobby,
      gameMode: gameModes[nextIndex]
    };

    this.updateLobbySettings(updateData);
  }

  updateLobbySettings(updateData: Partial<Lobby>): void {
    this.isLoading = true;
    this.subscriptions.push(
      this.lobbyService.updateLobbySettings(updateData).subscribe({
        next: () => {
          this.isLoading = false;
          this.snackBar.open('Lobby settings updated', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error updating lobby settings', error);
          this.snackBar.open('Failed to update lobby settings', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      })
    );
  }
}
