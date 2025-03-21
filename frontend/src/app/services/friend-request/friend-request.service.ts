import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {PROXY_API_URL} from '../../../environments/api-config';

export interface FriendRequest {
  id: string;
  fromId: string;
  toId: string;
  status: string;
}

export interface Friend {
  user1Id: string;
  user2Id: string;
}

@Injectable({
  providedIn: 'root'
})
export class FriendRequestService {
  private apiUrl = PROXY_API_URL + '/friends/request';
  private reloadSource = new Subject<void>();
  reload$ = this.reloadSource.asObservable();

  constructor(private http: HttpClient) {}

  sendFriendRequest(senderId: string, receiverUsername: string): Observable<any> {
    if (senderId === '' || receiverUsername === '') return new Observable();
    return this.http.post(`${this.apiUrl}/send`, null, {
      params: new HttpParams().set('senderId', senderId).set('receiverUsername', receiverUsername),
    });
  }

  getSentRequests(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/sent`, {
      params: new HttpParams().set('userId', userId),
    });
  }

  getIncomingRequests(userId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/requests`, {
      params: new HttpParams().set('userId', userId),
    });
  }

  acceptFriendRequest(requestId: number): Observable<any> {
    this.reloadSource.next();
    return this.http.post(`${this.apiUrl}/accept`, null, {
      params: new HttpParams().set('requestId', requestId.toString()),
    });
  }

  declineFriendRequest(requestId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/decline`, {
      params: new HttpParams().set('requestId', requestId.toString()),
    });
  }

  cancelFriendRequest(requestId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/cancel`, {
      params: new HttpParams().set('requestId', requestId.toString()),
    });
  }
}
