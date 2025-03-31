import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PROXY_API_URL} from '../../../environments/api-config';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ClubService {
  private apiUrl = PROXY_API_URL + '/clubs';

  constructor(private http : HttpClient) {}

  getClubs() {
    return this.http.get<any[]>(`${this.apiUrl}/list`);
  }

  getClub(id: number) {
    return this.http.get<any>(`${this.apiUrl}/get`, {
      params: {
        clubId: id
      }
    });
  }

  getPublicClubs() {
    return this.http.get<any[]>(`${this.apiUrl}/public`);
  }

  getJoinableClubs(userId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/joinable`, {
      params: {
        userId: userId
      }
    });
  }

  getClubMembers(id: number) {
    return this.http.get<any[]>(`${this.apiUrl}/members`, {
      params: {
        clubId: id
      }
    });
  }

  getClubsByUser(userId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/user`, {
      params: {
        userId: userId
      }
    });
  }

  createClub(name: string, description: string, isPublic : boolean, userId : number) :Observable<any> {
    return this.http.post<any[]>(`${this.apiUrl}/create`, null, {
      params: {
        name: name,
        description: description,
        isPublic: isPublic,
        userId: userId
      }
    });
  }

  updateClub(clubId: number, name: string, description: string, isPublic: boolean): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update`, null, {
      params: {
        clubId: clubId,
        name: name,
        description: description,
        isPublic: isPublic
      }
    });
  }

  deleteClub(id: number) {
    return this.http.delete<any[]>(`${this.apiUrl}/delete`, {
      params: {
        clubId: id
      }
    });
  }

  getClubById(clubId: number) {
    return this.http.get<any>(`${this.apiUrl}/get`, {
      params: {
        clubId: clubId
      }
    });
  }
}
