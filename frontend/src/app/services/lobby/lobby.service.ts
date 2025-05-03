import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {Lobby} from '../../models/lobby.model';
import {Game} from '../../models/game.model';
import {BACKEND_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root'
})
export class LobbyService {
  private apiUrl = BACKEND_API_URL + "/lobbies"
  private currentLobbySubject = new BehaviorSubject<Lobby | null>(null)
  currentLobby$ = this.currentLobbySubject.asObservable()

  constructor(private http: HttpClient) {}

  createLobby(leaderId: number, gameId: number, playWithPoints: boolean, isPublic = false): Observable<Lobby> {
    const params = new HttpParams()
      .set("leaderId", leaderId.toString())
      .set("gameId", gameId.toString())
      .set("playWithPoints", playWithPoints.toString())
      .set("isPublic", isPublic.toString())

    return this.http
      .post<Lobby>(`${this.apiUrl}/create`, null, { params })
      .pipe(tap((lobby) => this.currentLobbySubject.next(lobby)))
  }

  joinLobby(code: string, userId: number): Observable<Lobby> {
    const params = new HttpParams().set("code", code).set("userId", userId.toString())

    return this.http
      .post<Lobby>(`${this.apiUrl}/join`, null, { params })
      .pipe(tap((lobby) => this.currentLobbySubject.next(lobby)))
  }

  kickPlayer(lobbyId: number, leaderId: number, playerId: number): Observable<Lobby> {
    const params = new HttpParams()
      .set("lobbyId", lobbyId.toString())
      .set("leaderId", leaderId.toString())
      .set("playerId", playerId.toString())

    return this.http.post<Lobby>(`${this.apiUrl}/kick`, null, { params }).pipe(
      tap((lobby) => {
        if (this.currentLobbySubject.value?.id === lobbyId) {
          this.currentLobbySubject.next(lobby)
        }
      }),
    )
  }

  leaveLobby(lobbyId: number, userId: number): Observable<any> {
    const params = new HttpParams().set("lobbyId", lobbyId.toString()).set("userId", userId.toString())

    return this.http.post<any>(`${this.apiUrl}/leave`, null, { params }).pipe(
      tap((response) => {
        if (response.lobbyDeleted || this.currentLobbySubject.value?.id === lobbyId) {
          this.currentLobbySubject.next(null)
        } else if (!response.lobbyDeleted && response.lobby) {
          this.currentLobbySubject.next(response.lobby)
        }
      }),
    )
  }

  updateLobbySettings(lobbyId: number, leaderId: number, gameId: number, playWithPoints: boolean): Observable<Lobby> {
    const params = new HttpParams()
      .set("lobbyId", lobbyId.toString())
      .set("leaderId", leaderId.toString())
      .set("gameId", gameId.toString())
      .set("playWithPoints", playWithPoints.toString())

    return this.http.put<Lobby>(`${this.apiUrl}/update-settings`, null, { params }).pipe(
      tap((lobby) => {
        if (this.currentLobbySubject.value?.id === lobbyId) {
          this.currentLobbySubject.next(lobby)
        }
      }),
    )
  }

  startGame(lobbyId: number, leaderId: number): Observable<Lobby> {
    const params = new HttpParams().set("lobbyId", lobbyId.toString()).set("leaderId", leaderId.toString())

    return this.http.post<Lobby>(`${this.apiUrl}/start-game`, null, { params }).pipe(
      tap((lobby) => {
        if (this.currentLobbySubject.value?.id === lobbyId) {
          this.currentLobbySubject.next(lobby)
        }
      }),
    )
  }

  getLobby(lobbyId: number): Observable<Lobby> {
    const params = new HttpParams().set("lobbyId", lobbyId.toString())
    return this.http.get<Lobby>(`${this.apiUrl}/get`, { params }).pipe(
      tap((lobby) => {
        if (!this.currentLobbySubject.value || this.currentLobbySubject.value.id === lobbyId) {
          this.currentLobbySubject.next(lobby)
        }
      }),
    )
  }

  getLobbyByCode(code: string): Observable<Lobby> {
    const params = new HttpParams().set("code", code)
    return this.http.get<Lobby>(`${this.apiUrl}/get-by-code`, { params })
  }

  getLobbyByPlayer(userId: number): Observable<Lobby> {
    const params = new HttpParams().set("userId", userId.toString())
    return this.http.get<Lobby>(`${this.apiUrl}/player-lobby`, { params })
  }

  getAllGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.apiUrl}/games`)
  }

  getPublicLobbies(): Observable<Lobby[]> {
    return this.http.get<Lobby[]>(`${this.apiUrl}/public-lobbies`)
  }
}
