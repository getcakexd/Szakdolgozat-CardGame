import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PROXY_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root'
})
export class FriendshipService {
  private apiUrl = PROXY_API_URL + '/friends';

  constructor(private http : HttpClient) {}

  getFriends(userId : string) : Observable<any> {
    return this.http.get<any[]>(`${this.apiUrl}/list`, {
      params: new HttpParams().set('userId', userId),
    });
  }

  deleteFriend(userId: string, friendId: string) : Observable<any>{
    return this.http.delete(`${this.apiUrl}/remove`, {
      params: new HttpParams().set('userId', userId).set('friendId', friendId),
    });
  }
}
