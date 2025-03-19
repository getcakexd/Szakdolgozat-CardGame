import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {Router} from '@angular/router';

export interface User {
  username: string;
  password: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:4200/api/users';
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient,  private router: Router) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/all`);
  }

  getUserById(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/get`, { params: { userId } });
  }

  createUser(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, user);
  }

  login(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user).pipe(
      tap((response : any) => {
        localStorage.setItem('id', response.userId);
        localStorage.setItem('username', user.username);
        localStorage.setItem('password', user.password);
        localStorage.setItem('email', response.email);
        this.isLoggedInSubject.next(true);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('id');
    localStorage.removeItem('username');
    localStorage.removeItem('password');
    localStorage.removeItem('email');
    this.router.navigate(['/home']).then(() => {
      window.location.reload();
    });
    this.isLoggedInSubject.next(false);
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('id') !== null;
  }

  updateUsername(userId: string, newUsername: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/username`, null, { params: { userId, newUsername } });
  }

  updateEmail(userId: string, newEmail: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/email`, null, { params: { userId, newEmail } });
  }

  updatePassword(userId: string, currentPassword: string, newPassword: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/update/password`, null, { params: { userId, currentPassword, newPassword } });
  }

  deleteAccount(userId: string, password: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete`, { params: { userId, password } });
  }

  getLoggedInId(): string {
    let id = localStorage.getItem('id');
    if (id !== null) return id;
    return '';
  }

  getLoggedInUsername(): string {
    let username = localStorage.getItem('usernamerivc');
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
}
