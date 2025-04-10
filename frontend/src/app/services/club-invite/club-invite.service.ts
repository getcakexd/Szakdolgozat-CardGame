import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {Observable} from 'rxjs';
import {ClubInvite} from '../../models/club-invite.model';

@Injectable({
  providedIn: 'root'
})
export class ClubInviteService {
  private apiUrl = BACKEND_API_URL + '/clubinvites';

  constructor(private http : HttpClient) { }

  getUserInvites(userId: number) : Observable<ClubInvite[]> {
    return this.http.get<ClubInvite[]>(`${this.apiUrl}/list`,
      {
        params: {
          userId: userId
        }
      }
    );
  }

  acceptInvite(id: number) {
    return this.http.put(`${this.apiUrl}/accept`, null, {
      params: {
        id: id
      }
    });

  }

  declineInvite(id: number) {
    return this.http.put(`${this.apiUrl}/decline`, null, {
      params: {
        id: id
      }
    });
  }

  inviteUser(clubId: number, username: string) {
    return this.http.post(`${this.apiUrl}/add`, null, {
      params: {
        clubId: clubId,
        username: username
      }
    });
  }

  getPendingInvites(clubId: number) {
    return this.http.get<ClubInvite[]>(`${this.apiUrl}/pending`, {
      params: {
        clubId: clubId
      }
    });
  }

  getInviteHistory(clubId: number) {
    return this.http.get<ClubInvite[]>(`${this.apiUrl}/history`, {
      params: {
        clubId: clubId
      }
    });
  }

  cancelInvite(inviteId: number) {
    return this.http.delete<any>(`${this.apiUrl}/delete`, {
      params: {
        id: inviteId
      }
    });
  }
}
