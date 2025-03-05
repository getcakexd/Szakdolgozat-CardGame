import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

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

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/all`);
  }

  login(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user);
  }

  createUser(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/create`, user);
  }
}
