import { Injectable } from '@angular/core';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {GameStatistics, LeaderboardEntry, UserGameStats, UserStats} from '../../models/user-stats.model';
import {map} from 'rxjs/operators';
import {GameService} from '../game/game.service';
import {Game} from '../../models/game.model';
import {ClubGameStats, ClubMemberStats, ClubStats} from '../../models/club-stats.model';

@Injectable({
  providedIn: 'root'
})
export class StatsService {
  private apiUrl = BACKEND_API_URL + "/stats"
  private gamesCache = new Map<number, Game>()

  constructor(
    private http: HttpClient,
    private gameService: GameService,
  ) {}

  getUserStats(userId: number): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.apiUrl}/user/${userId}`)
  }

  getUserGameStats(userId: number, gameType?: string): Observable<UserGameStats[]> {
    let url = `${this.apiUrl}/user/${userId}/games`
    if (gameType) {
      url += `?gameType=${gameType}`
    }
    return this.http.get<UserGameStats[]>(url).pipe(
      map((stats) => {
        return stats
      }),
    )
  }

  getRecentGames(userId: number, limit = 10): Observable<GameStatistics[]> {
    return this.http.get<GameStatistics[]>(`${this.apiUrl}/user/${userId}/recent?limit=${limit}`).pipe(
      map((games) => {
        return games.map((game) => {
          if (game.gameDefinitionId) {
            this.getGameName(game.gameDefinitionId).subscribe((name) => {
              game.gameType = name
            })
          }
          return game
        })
      }),
    )
  }

  getLeaderboard(gameId?: number, limit = 10): Observable<LeaderboardEntry[]> {
    let url = `${this.apiUrl}/leaderboard?limit=${limit}`
    if (gameId) {
      url += `&gameId=${gameId}`
    }
    return this.http.get<LeaderboardEntry[]>(url)
  }

  getClubStats(clubId: number): Observable<ClubStats> {
    return this.http.get<ClubStats>(`${this.apiUrl}/club/${clubId}`)
  }

  getClubGameStats(clubId: number): Observable<ClubGameStats[]> {
    return this.http.get<ClubGameStats[]>(`${this.apiUrl}/club/${clubId}/games`)
  }

  getClubGameStatsByGame(clubId: number, gameId: number): Observable<ClubGameStats> {
    return this.http.get<ClubGameStats>(`${this.apiUrl}/club/${clubId}/game/${gameId}`)
  }

  getClubMemberStats(clubId: number): Observable<ClubMemberStats[]> {
    return this.http.get<ClubMemberStats[]>(`${this.apiUrl}/club/${clubId}/members`)
  }

  getClubMemberStatsByUser(clubId: number, userId: number): Observable<ClubMemberStats> {
    return this.http.get<ClubMemberStats>(`${this.apiUrl}/club/${clubId}/member/${userId}`)
  }

  getClubLeaderboard(limit = 10): Observable<ClubGameStats[]> {
    return this.http.get<ClubGameStats[]>(`${this.apiUrl}/club/leaderboard?limit=${limit}`)
  }

  getClubLeaderboardByGame(gameId: number, limit = 10): Observable<ClubGameStats[]> {
    return this.http.get<ClubGameStats[]>(`${this.apiUrl}/club/leaderboard/game/${gameId}?limit=${limit}`)
  }

  getClubMemberLeaderboard(clubId: number, limit = 10): Observable<ClubMemberStats[]> {
    return this.http.get<ClubMemberStats[]>(`${this.apiUrl}/club/${clubId}/leaderboard?limit=${limit}`)
  }

  getClubMemberLeaderboardByGame(clubId: number, gameId: number, limit = 10): Observable<ClubMemberStats[]> {
    return this.http.get<ClubMemberStats[]>(`${this.apiUrl}/club/${clubId}/leaderboard/game/${gameId}?limit=${limit}`)
  }

  getAllGames(): Observable<Game[]> {
    return this.gameService.getActiveGames()
  }

  getGameName(gameId: number): Observable<string> {
    if (this.gamesCache.has(gameId)) {
      return of(this.gamesCache.get(gameId)!.name)
    }

    return this.gameService.getGameById(gameId).pipe(
      map((game) => {
        this.gamesCache.set(gameId, game)
        return game.name
      }),
    )
  }
}
