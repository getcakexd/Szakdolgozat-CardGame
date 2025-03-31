import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {PROXY_API_URL} from '../../../environments/api-config';
import {FriendRequest} from '../../models/FriendRequest';



@Injectable({
  providedIn: 'root'
})
export class FriendRequestService {
  private apiUrl = PROXY_API_URL + '/friends/request';
  private reloadSource = new Subject<void>();
  reload$ = this.reloadSource.asObservable();

  constructor(private http: HttpClient) {}

  sendFriendRequest(senderId: number, receiverUsername: string): Observable<any> {
    if (receiverUsername === '') return new Observable();
    return this.http.post(`${this.apiUrl}/send`, null, {
      params: {
        senderId: senderId,
        receiverUsername: receiverUsername,
      }
    });
  }

  getSentRequests(userId: number): Observable<FriendRequest[]> {
    return this.http.get<FriendRequest[]>(`${this.apiUrl}/sent`, {
      params: {
        userId: userId,
      },
    });
  }

  getIncomingRequests(userId: number): Observable<FriendRequest[]> {
    return this.http.get<FriendRequest[]>(`${this.apiUrl}/requests`, {
      params: {
        userId: userId,
      },
    });
  }

  acceptFriendRequest(requestId: number): Observable<any> {
    this.reloadSource.next();
    return this.http.post(`${this.apiUrl}/accept`, null, {
      params: {
        requestId: requestId,
      },
    });
  }

  declineFriendRequest(requestId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/decline`, {
      params: {
        requestId: requestId,
      },
    });
  }

  cancelFriendRequest(requestId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/cancel`, {
      params: {
        requestId: requestId,
      },
    });
  }
}
