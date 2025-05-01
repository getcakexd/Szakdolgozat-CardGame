import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable } from "rxjs"
import { Game, GameCreationDTO } from '../../models/game.model';

@Injectable({
  providedIn: "root",
})
export class GameService {
  private adminApiUrl = "/api/admin/games"
  private gameApiUrl = "/api/games"

  constructor(private http: HttpClient) {}

  getAllGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.adminApiUrl}/all`)
  }

  getActiveGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.gameApiUrl}/active`)
  }

  getGameById(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.gameApiUrl}/${id}`)
  }

  getGameByName(name: string): Observable<Game> {
    return this.http.get<Game>(`${this.gameApiUrl}/name/${name}`)
  }

  getGameDescriptionByLanguage(gameId: number, language: string): Observable<any> {
    return this.http.get<any>(`${this.gameApiUrl}/${gameId}/description/${language}`)
  }

  getGameRulesByLanguage(gameId: number, language: string): Observable<any> {
    return this.http.get<any>(`${this.gameApiUrl}/${gameId}/rules/${language}`)
  }

  createGame(game: GameCreationDTO): Observable<any> {
    console.log(game)
    return this.http.post(`${this.adminApiUrl}/create`, game)
  }

  updateGame(id: number, game: GameCreationDTO): Observable<any> {
    return this.http.put(`${this.adminApiUrl}/update/${id}`, game)
  }

  deleteGame(gameId: number): Observable<any> {
    return this.http.delete(`${this.adminApiUrl}/delete?gameId=${gameId}`)
  }
}
