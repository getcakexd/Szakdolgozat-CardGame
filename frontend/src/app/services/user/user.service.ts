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
  private agentApiUrl = BACKEND_API_URL + '/agent/users';

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
    const passwordValidation = this.validatePasswordComplexity(user.password);

    if (!passwordValidation.valid) {
      return new Observable(observer => {
        observer.error({ error: { message: passwordValidation.message } });
      });
    }

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
    const passwordValidation = this.validatePasswordComplexity(newPassword);

    if (!passwordValidation.valid) {
      return new Observable(observer => {
        observer.error({ error: { message: passwordValidation.message } });
      });
    }

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

  hasGoogleAuthPassword(userId: number): Observable<{ hasGoogleAuthPassword: boolean }> {
    return this.http.get<{ hasGoogleAuthPassword: boolean }>(`${this.apiUrl}/has-google-auth-password`, {
      params: {
        userId,
      },
    })
  }

  setPassword(userId: number, newPassword: string): Observable<any> {
    const passwordValidation = this.validatePasswordComplexity(newPassword);

    if (!passwordValidation.valid) {
      return new Observable(observer => {
        observer.error({ error: { message: passwordValidation.message } });
      });
    }

    return this.http.put(`${this.apiUrl}/set-password`, null, {
      params: {
        userId,
        newPassword,
      },
      responseType: "text",
    });
  }

  private validatePasswordComplexity(password: string): { valid: boolean; message?: string } {
    const minLength = 8;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumeric = /[0-9]/.test(password);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);

    if (password.length < minLength) {
      return { valid: false, message: 'Password must be at least 8 characters long' };
    }

    if (!hasUpperCase) {
      return { valid: false, message: 'Password must contain at least one uppercase letter' };
    }

    if (!hasLowerCase) {
      return { valid: false, message: 'Password must contain at least one lowercase letter' };
    }

    if (!hasNumeric) {
      return { valid: false, message: 'Password must contain at least one number' };
    }

    if (!hasSpecialChar) {
      return { valid: false, message: 'Password must contain at least one special character' };
    }

    return { valid: true };
  }

  adminCreateUser(user: User): Observable<any> {
    return this.http.post(`${this.adminApiUrl}/users/create`, user)
  }

  adminDeleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.adminApiUrl}/users/delete?userId=${userId}`)
  }

  promoteToAgent(userId: number): Observable<any> {
    return this.http.put(`${this.adminApiUrl}/users/promote-to-agent?userId=${userId}`, {})
  }

  demoteFromAgent(userId: number): Observable<any> {
    return this.http.put(`${this.adminApiUrl}/users/demote-from-agent?userId=${userId}`, {})
  }

  promoteToAdmin(userId: number): Observable<any> {
    return this.http.put(`${this.adminApiUrl}/users/promote-to-admin?userId=${userId}`, {})
  }

  demoteFromAdmin(userId: number): Observable<any> {
    return this.http.put(`${this.adminApiUrl}/users/demote-from-admin?userId=${userId}`, {})
  }

  formatRole(role: string): string {
    return role.replace("ROLE_", "")
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
