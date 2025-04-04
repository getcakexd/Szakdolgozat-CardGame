import { Injectable } from '@angular/core';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {User} from '../../models/user.model';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = BACKEND_API_URL + '/users';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    const storedUser = localStorage.getItem("currentUser")
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser))
    }
  }

  login(username: string, password: string): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/login`, { username, password }).pipe(
      tap((user) => {
        localStorage.setItem("currentUser", JSON.stringify(user))
        this.currentUserSubject.next(user)
      }),
    )
  }

  logout(): void {
    localStorage.removeItem("currentUser");
    this.currentUserSubject.next(null);
    this.router.navigate(['/home']).then(() => {
      window.location.reload();
    });
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('currentUser') !== null;
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserId(): number | null {
    return this.currentUser?.id || null;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === "ROLE_ADMIN"
  }

  isAgent(): boolean {
    return this.currentUser?.role === "ROLE_AGENT"
  }
}
