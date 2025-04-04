import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {Router} from '@angular/router';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {User} from '../../models/user.model';
import {UserHistory} from '../../models/user-history.model';



@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = BACKEND_API_URL + '/users';
  private adminApiUrl = BACKEND_API_URL + '/admin';
  private agentApiUrl = BACKEND_API_URL + '/agent';

  constructor(private http: HttpClient, private router: Router) {}

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
    localStorage.removeItem('currentUser');
    return this.http.delete(`${this.apiUrl}/delete`, {
      params: {
        userId,
        password
      }
    });
  }

  adminCreateUser(user: User): Observable<any> {
    return this.http.post(`${this.adminApiUrl}/create`, user)
  }

  adminDeleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.adminApiUrl}/delete?userId=${userId}`)
  }

  unlockUser(userId: number): Observable<any> {
    return this.http.put(`${this.agentApiUrl}/unlock?userId=${userId}`, {})
  }

  modifyUserData(userId: number, userData: Partial<User>): Observable<any> {
    return this.http.put(`${this.agentApiUrl}/modify`, { userId, ...userData })
  }

  getUserHistory(userId: number): Observable<UserHistory[]> {
    return this.http.get<UserHistory[]>(`${this.apiUrl}/history?userId=${userId}`)
  }
}
