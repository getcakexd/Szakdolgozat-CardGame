import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable } from "rxjs"
import {Game} from '../../models/game.model';

@Injectable({
  providedIn: "root",
})
export class GameService {
  private apiUrl = "/api/admin/games"

  constructor(private http: HttpClient) {}

  getAllGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.apiUrl}/all`)
  }

  getGameById(id: number): Observable<Game> {
    return this.http.get<Game>(`${this.apiUrl}/get?gameId=${id}`)
  }

  createGame(game: Game): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, game)
  }

  updateGame(game: Game): Observable<any> {
    return this.http.put(`${this.apiUrl}/update`, game)
  }

  deleteGame(gameId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete?gameId=${gameId}`)
  }
}

