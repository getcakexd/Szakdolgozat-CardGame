import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {Router} from '@angular/router';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {User} from '../../models/user.model';



@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = BACKEND_API_URL + '/users';
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  isLoggedIn$ = this.isLoggedInSubject.asObservable();


  constructor(private http: HttpClient,  private router: Router) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/all`);
  }

  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/get`, {
      params: {
        userId
      }
    });
  }

  createUser(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, user);
  }

  login(user: User): Observable<any> {
    return this.http.post<User>(`${this.apiUrl}/login`, user).pipe(
      tap({
        next: (response: User) => {
          localStorage.setItem('id', response.id.toString());
          localStorage.setItem('username', response.username);
          localStorage.setItem('email', response.email);
          localStorage.setItem('password', response.password);
          localStorage.setItem('role', response.role);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('id');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('password');
    localStorage.removeItem('role');
    this.router.navigate(['/home']).then(() => {
      window.location.reload();
    });
    this.isLoggedInSubject.next(false);
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('id') !== null;
  }

  updateUsername(userId: number, newUsername: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/username`, null, {
      params: {
        userId,
        newUsername
      },
      responseType: 'text'
    });
  }

  updateEmail(userId: number, newEmail: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/email`, null, {
      params: {
        userId,
        newEmail
      },
      responseType: 'text'
    });
  }

  updatePassword(userId: number, currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/password`, null, {
      params: {
        userId,
        currentPassword,
        newPassword
      },
      responseType: 'text'
    });
  }

  deleteAccount(userId: number, password: string): Observable<any> {
    localStorage.removeItem('id');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    localStorage.removeItem('password');
    localStorage.removeItem('role');
    return this.http.delete(`${this.apiUrl}/delete`, {
      params: {
        userId,
        password
      }
    });
  }

  getLoggedInId(): number {
    return parseInt(localStorage.getItem('id') || '0');
  }

  getLoggedInUsername(): string {
    let username = localStorage.getItem('username');
    if (username !== null) return username;
    return '';
  }

  setLoggedInUsername(username: string): void {
    localStorage.setItem('username', username);
  }

  getLoggedInEmail(): string {
    let email = localStorage.getItem('email');
    if (email !== null) return email;
    return '';
  }

  setLoggedInEmail(email: string): void {
    localStorage.setItem('email', email);
  }

  getLoggedInPassword(): string {
    let password = localStorage.getItem('password');
    if (password !== null) return password;
    return '';
  }

  setLoggedInPassword(password: string): void {
    localStorage.setItem('password', password);
  }

  getLoggedInRole(): string {
    let role = localStorage.getItem('role');
    if (role !== null) return role;
    return '';
  }
}
