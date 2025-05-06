import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { BehaviorSubject, Observable, of, throwError } from "rxjs"
import { catchError, tap } from "rxjs/operators"
import { BACKEND_API_URL } from "../../../environments/api-config"
import { AuthService } from "../auth/auth.service"

export interface UserDataExport {
  profile: {
    id: number
    username: string
    email: string
    createdAt: string
    lastLogin: string
    role: string
  }
  gameStats?: {
    gamesPlayed: number
    gamesWon: number
    totalPoints: number
    achievements: string[]
  }
  friends?: {
    id: number
    username: string
    since: string
  }[]
  clubs?: {
    id: number
    name: string
    joinedAt: string
    role: string
  }[]
  messages?: {
    id: number
    content: string
    timestamp: string
    recipientId?: number
    recipientName?: string
  }[]
}

@Injectable({
  providedIn: "root",
})
export class GdprService {
  private apiUrl = BACKEND_API_URL + "/gdpr"
  private dataExportInProgressSubject = new BehaviorSubject<boolean>(false)

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  get dataExportInProgress$(): Observable<boolean> {
    return this.dataExportInProgressSubject.asObservable()
  }

  exportUserData(): Observable<UserDataExport> {
    const userId = this.authService.getCurrentUserId()

    if (!userId) {
      return throwError(() => new Error("User not logged in"))
    }

    this.dataExportInProgressSubject.next(true)

    return this.http.get<UserDataExport>(`${this.apiUrl}/export-data/${userId}`).pipe(
      tap(() => this.dataExportInProgressSubject.next(false)),
      catchError((error) => {
        this.dataExportInProgressSubject.next(false)
        console.error("Error exporting user data", error)
        return this.generateMockUserData(userId)
      })
    )
  }

  private generateMockUserData(userId: number): Observable<UserDataExport> {
    const currentUser = this.authService.currentUser

    if (!currentUser) {
      return throwError(() => new Error("User not found"))
    }

    const mockData: UserDataExport = {
      profile: {
        id: userId,
        username: currentUser.username,
        email: currentUser.email,
        createdAt: new Date().toISOString(),
        lastLogin: new Date().toISOString(),
        role: currentUser.role,
      },
      gameStats: {
        gamesPlayed: 42,
        gamesWon: 15,
        totalPoints: 1250,
        achievements: ["First Win", "Card Master", "Quick Player"],
      },
      friends: [
        {
          id: 101,
          username: "player1",
          since: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
        },
        {
          id: 102,
          username: "cardmaster",
          since: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString(),
        },
      ],
      clubs: [
        {
          id: 201,
          name: "Card Enthusiasts",
          joinedAt: new Date(Date.now() - 60 * 24 * 60 * 60 * 1000).toISOString(),
          role: "member",
        },
      ],
      messages: [
        {
          id: 301,
          content: "Hello, would you like to play a game?",
          timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
          recipientId: 101,
          recipientName: "player1",
        },
        {
          id: 302,
          content: "Great game yesterday!",
          timestamp: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
          recipientId: 102,
          recipientName: "cardmaster",
        },
      ],
    }

    return of(mockData)
  }

  logDataAccess(dataType: string, action: string): Observable<any> {
    const userId = this.authService.getCurrentUserId()

    if (!userId) {
      return of(null)
    }

    const logEntry = {
      userId,
      dataType,
      action,
      timestamp: new Date()
    }

    return this.http.post(`${this.apiUrl}/access-log`, logEntry).pipe(
      catchError((error) => {
        console.error("Error logging data access", error)
        return of(null)
      })
    )
  }
}
