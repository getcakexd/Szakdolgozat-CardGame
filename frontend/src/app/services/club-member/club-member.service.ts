import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {ClubMember} from '../../models/club-member.model';

@Injectable({
  providedIn: 'root'
})
export class ClubMemberService {
  private apiUrl = BACKEND_API_URL + '/clubmembers';

  constructor(private http: HttpClient) { }

  addMember(clubId: number, userId: number) {
    return this.http.post(`${this.apiUrl}/add`, null, {
      params: {
        clubId: clubId,
        userId: userId
      }
    });
  }

  getMembers(clubId: number) {
    return this.http.get<ClubMember[]>(`${this.apiUrl}/list`, {
      params: {
        clubId: clubId
      }
    });
  }

  getUserRole(clubId: number, userId: number) {
    return this.http.get<any>(`${this.apiUrl}/role`, {
      params: {
        clubId: clubId,
        userId: userId
      }
    });
  }

  kickMember(id: number, userId: number) {
    return this.http.delete(`${this.apiUrl}/kick`, {
      params: {
        clubId: id,
        userId: userId
      }
    });
  }

  leaveClub(clubId: number, userId: number) {
    return this.http.delete<any>(`${this.apiUrl}/leave`, {
      params: {
        clubId: clubId,
        userId: userId
      }
    });
  }

  updateMemberRole(clubId: number, userId: number, newRole: string) {
    return this.http.put(`${this.apiUrl}/modify`, null, {
      params: {
        clubId: clubId,
        userId: userId,
        role: newRole
      }
    });
  }
}
