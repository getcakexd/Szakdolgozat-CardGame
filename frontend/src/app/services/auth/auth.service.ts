import { Injectable } from '@angular/core';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {BehaviorSubject, Observable, of, tap} from 'rxjs';
import {User} from '../../models/user.model';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {SocialAuthService, SocialUser} from '@abacritt/angularx-social-login';

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private apiUrl = BACKEND_API_URL + "/users"
  private currentUserSubject = new BehaviorSubject<User | null>(null)
  public currentUser$ = this.currentUserSubject.asObservable()
  private googleAuthInProgress = false

  constructor(
    private http: HttpClient,
    private router: Router,
    private socialAuthService: SocialAuthService,
  ) {
    const storedUser = localStorage.getItem("currentUser")
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser))
    }
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((response) => {
        if (response && !response.unverified) {
          localStorage.setItem("currentUser", JSON.stringify(response))
          this.currentUserSubject.next(response)
        }
      }),
    )
  }

  loginWithGoogle(socialUser: SocialUser): Observable<User> {
    if (this.googleAuthInProgress) {
      return of() as Observable<User>
    }

    this.googleAuthInProgress = true

    return this.http
      .post<User>(`${this.apiUrl}/google-auth`, {
        email: socialUser.email,
        name: socialUser.name,
        token: socialUser.idToken,
        photoUrl: socialUser.photoUrl,
      })
      .pipe(
        tap((user) => {
          localStorage.setItem("currentUser", JSON.stringify(user))
          this.currentUserSubject.next(user)
          this.googleAuthInProgress = false
        }),
      )
  }

  logout(): void {
    localStorage.removeItem("currentUser")
    this.currentUserSubject.next(null)
    this.socialAuthService.signOut().catch(() => {
    })
    this.router.navigate(["/home"]).then(() => {
      window.location.reload()
    })
  }

  isLoggedIn(): boolean {
    return localStorage.getItem("currentUser") !== null
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value
  }

  getCurrentUserId(): number | null {
    return this.currentUser?.id || null
  }

  isAdmin(): boolean {
    return this.currentUser?.role === "ROLE_ADMIN"
  }

  isAgent(): boolean {
    return this.currentUser?.role === "ROLE_AGENT"
  }

  isRoot(): boolean {
    return this.currentUser?.role === "ROLE_ROOT"
  }

  resendVerificationEmail(userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/resend-verification`, null, {
      params: { userId: userId.toString() }
    });
  }

  verifyEmail(token: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/verify-email`, {
      params: { token }
    });
  }
}
