import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PROXY_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root'
})
export class ClubMemberService {
  private apiUrl = PROXY_API_URL + '/clubmembers';

  constructor(private http: HttpClient) { }

  addMember(clubId: number, userId: number) {
    return this.http.post<any[]>(`${this.apiUrl}/add`, null, {
      params: {clubId: clubId, userId: userId},
    });
  }

  getMembers(clubId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/list`, {
      params: {clubId: clubId},
    });
  }

  getUserRole(clubId: number, userId: number) {
    return this.http.get<any>(`${this.apiUrl}/role`, {
      params: {clubId: clubId, userId: userId},
    });
  }

  kickMember(id: number, userId: number) {
    return this.http.delete<any[]>(`${this.apiUrl}/kick`, {
      params: {clubId: id, userId: userId},
    });
  }

  updateMemberRole(clubId: number, userId: number, newRole: string) {
    return this.http.put<any[]>(`${this.apiUrl}/modify`, null, {
      params: {clubId: clubId, userId: userId, role: newRole},
    });
  }
}
