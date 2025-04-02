import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Lobby } from '../../models/lobby.model';
import { LobbyMessage } from '../../models/LobbyMessage.model';
import { WebSocketService } from '../websocket/websocket.service';
import { UserService } from '../user/user.service';
import { BACKEND_API_URL } from '../../../environments/api-config';
import { LobbyPlayer } from '../../models/LobbyPlayer.model';
import { User } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class LobbyService {
  private currentLobbySubject = new BehaviorSubject<Lobby | null>(null);
  public currentLobby$ = this.currentLobbySubject.asObservable();

  private lobbyMessagesSubject = new BehaviorSubject<LobbyMessage[]>([]);
  public lobbyMessages$ = this.lobbyMessagesSubject.asObservable();

  private apiUrl = BACKEND_API_URL + '/lobby';
  private wsSubscribed = false;
  private currentUser: User | null = null;
  private sessionId: string;

  constructor(
    private http: HttpClient,
    private webSocketService: WebSocketService,
    private userService: UserService
  ) {
    // Generate a session ID for this client
    this.sessionId = this.generateSessionId();

    // Subscribe to WebSocket connection state
    this.webSocketService.connected$.subscribe((connected: boolean) => {
      if (connected && this.currentLobbySubject.value && !this.wsSubscribed) {
        this.setupWebSocketSubscriptions();
      }
    });

    // Load current user
    this.userService.getUserById(this.userService.getLoggedInId()).subscribe((user) => {
      this.currentUser = user;
      // Load current lobby after user is loaded
      this.getCurrentLobby().subscribe();
    });
  }

  private generateSessionId(): string {
    return 'session_' + Math.random().toString(36).substring(2, 15);
  }

  private getSessionHeaders(): HttpHeaders {
    return new HttpHeaders({
      'X-Session-ID': this.sessionId
    });
  }

  private setupWebSocketSubscriptions(): void {
    if (!this.currentLobbySubject.value || this.wsSubscribed) return;

    const lobbyId = this.currentLobbySubject.value.id;

    // Subscribe to lobby updates
    this.webSocketService.subscribe(`/topic/lobby/${lobbyId}/updates`, (lobby: Lobby) => {
      console.log('Received lobby update', lobby);
      this.currentLobbySubject.next(lobby);
    });

    // Subscribe to new messages
    this.webSocketService.subscribe(`/topic/lobby/${lobbyId}/messages`, (message: LobbyMessage) => {
      console.log('Received new message', message);
      const currentMessages = this.lobbyMessagesSubject.value;
      this.lobbyMessagesSubject.next([...currentMessages, message]);
    });

    // Subscribe to player kicked event
    this.webSocketService.subscribe(`/user/queue/lobby/kicked`, (data: any) => {
      console.log('You have been kicked from the lobby', data);
      this.currentLobbySubject.next(null);
      this.lobbyMessagesSubject.next([]);
    });

    this.wsSubscribed = true;
    console.log('WebSocket subscriptions set up for lobby', lobbyId);
  }

  public getCurrentLobby(): Observable<Lobby | null> {
    if (!this.currentUser) {
      return of(null);
    }

    return this.http.get<Lobby>(`${this.apiUrl}/current`, {
      params: {
        userId: this.userService.getLoggedInId().toString()
      }
    }).pipe(
      tap((lobby: Lobby) => {
        this.currentLobbySubject.next(lobby);
        // Set up WebSocket subscriptions if connected
        if (this.webSocketService.isConnected()) {
          this.setupWebSocketSubscriptions();
        }
        // Load messages for this lobby
        this.getLobbyMessages(lobby.id).subscribe();
      }),
      catchError((error: any) => {
        console.log('User is not in a lobby or error occurred', error);
        this.currentLobbySubject.next(null);
        return of(null);
      })
    );
  }

  public getPublicLobbies(): Observable<Lobby[]> {
    return this.http.get<Lobby[]>(`${this.apiUrl}/public`);
  }

  public getLobbyByCode(code: string): Observable<Lobby> {
    return this.http.get<Lobby>(`${this.apiUrl}/code/${code}`);
  }

  public createLobby(lobbyData: Partial<Lobby>): Observable<Lobby> {
    return this.http.post<Lobby>(
      `${this.apiUrl}/create`,
      lobbyData,
      {
        headers: this.getSessionHeaders(),
        params: {
          userId: this.userService.getLoggedInId().toString()
        }
      }
    ).pipe(
      tap((lobby: Lobby) => {
        this.currentLobbySubject.next(lobby);
        this.wsSubscribed = false;
        if (this.webSocketService.isConnected()) {
          this.setupWebSocketSubscriptions();
        }
      })
    );
  }

  public joinLobbyByCode(code: string): Observable<Lobby> {
    return this.http.post<Lobby>(
      `${this.apiUrl}/join/${code}`,
      {},
      {
        headers: this.getSessionHeaders(),
        params: {
          userId: this.userService.getLoggedInId().toString()
        }
      }
    ).pipe(
      tap((lobby: Lobby) => {
        this.currentLobbySubject.next(lobby);
        this.wsSubscribed = false;
        if (this.webSocketService.isConnected()) {
          this.setupWebSocketSubscriptions();
        }
        // Load messages for this lobby
        this.getLobbyMessages(lobby.id).subscribe();
      })
    );
  }

  public leaveLobby(): Observable<any> {
    const lobby = this.currentLobbySubject.value;
    if (!lobby) return of(null);

    return this.http.post(
      `${this.apiUrl}/${lobby.id}/leave`,
      {},
      {
        params: {
          userId: this.userService.getLoggedInId().toString()
        }
      }
    ).pipe(
      tap(() => {
        this.currentLobbySubject.next(null);
        this.lobbyMessagesSubject.next([]);
        this.wsSubscribed = false;
      })
    );
  }

  public kickPlayer(targetUserId: number): Observable<any> {
    const lobby = this.currentLobbySubject.value;
    if (!lobby) return of(null);

    return this.http.post(
      `${this.apiUrl}/${lobby.id}/kick`,
      {},
      {
        params: {
          leaderId: this.userService.getLoggedInId().toString(),
          targetUserId: targetUserId.toString()
        }
      }
    );
  }

  public toggleReady(): Observable<any> {
    const lobby = this.currentLobbySubject.value;
    if (!lobby) return of(null);

    return this.http.post(
      `${this.apiUrl}/${lobby.id}/ready`,
      {},
      {
        params: {
          userId: this.userService.getLoggedInId().toString()
        }
      }
    );
  }

  public updateLobbySettings(updateData: Partial<Lobby>): Observable<any> {
    const lobby = this.currentLobbySubject.value;
    if (!lobby) return of(null);

    return this.http.put<Lobby>(
      `${this.apiUrl}/${lobby.id}/settings`,
      updateData,
      {
        params: {
          leaderId: this.userService.getLoggedInId().toString()
        }
      }
    ).pipe(
      tap((updatedLobby: Lobby) => {
        this.currentLobbySubject.next(updatedLobby);
      })
    );
  }

  public getLobbyMessages(lobbyId: number): Observable<LobbyMessage[]> {
    return this.http.get<LobbyMessage[]>(`${this.apiUrl}/${lobbyId}/messages`).pipe(
      tap((messages: LobbyMessage[]) => {
        this.lobbyMessagesSubject.next(messages);
      })
    );
  }

  public sendMessage(content: string): Observable<any> {
    const lobby = this.currentLobbySubject.value;
    if (!lobby) return of(null);

    return this.http.post<LobbyMessage>(
      `${this.apiUrl}/${lobby.id}/messages`,
      { content },
      {
        params: {
          userId: this.userService.getLoggedInId().toString()
        }
      }
    );
  }

  public handleDisconnect(): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/disconnect`,
      {},
      {
        headers: this.getSessionHeaders()
      }
    );
  }

  public isUserLobbyLeader(): boolean {
    const lobby = this.currentLobbySubject.value;
    if (!lobby || !this.currentUser) return false;


    // @ts-ignore
    return lobby.players.find((player: LobbyPlayer) => player.user.id === this.currentUser.id)?.isLeader || false;
  }

  public getCurrentUserInLobby(): LobbyPlayer | null {
    const lobby = this.currentLobbySubject.value;
    if (!lobby || !this.currentUser) return null;

    // @ts-ignore
    return lobby.players.find((player: LobbyPlayer) => player.user.id === this.currentUser.id) || null;
  }

  public areAllPlayersReady(): boolean {
    const lobby = this.currentLobbySubject.value;
    if (!lobby || lobby.players.length < 2) return false;

    // Check if all non-leader players are ready
    const nonLeaderPlayers = lobby.players.filter((player: LobbyPlayer) => !player.isLeader);
    return nonLeaderPlayers.every((player: LobbyPlayer) => player.isReady);
  }
}
