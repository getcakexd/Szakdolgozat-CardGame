import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { BehaviorSubject, Observable, throwError } from "rxjs"
import { catchError, finalize } from "rxjs/operators"
import { BACKEND_API_URL } from "../../../environments/api-config"
import { AuthService } from "../auth/auth.service"

@Injectable({
  providedIn: "root",
})
export class GdprService {
  private apiUrl = BACKEND_API_URL + "/users"
  private dataExportInProgressSubject = new BehaviorSubject<boolean>(false)

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  get dataExportInProgress$(): Observable<boolean> {
    return this.dataExportInProgressSubject.asObservable()
  }

  exportUserData(): Observable<any> {
    const userId = this.authService.getCurrentUserId()

    if (!userId) {
      return throwError(() => new Error("User not logged in"))
    }

    this.dataExportInProgressSubject.next(true)

    return this.http.get<any>(`${this.apiUrl}/export-data/${userId}`).pipe(
      finalize(() => this.dataExportInProgressSubject.next(false)),
      catchError((error) => {
        console.error("Error exporting user data:", error)
        return throwError(() => new Error("Failed to export user data"))
      }),
    )
  }
}
