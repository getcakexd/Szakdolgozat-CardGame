import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PROXY_API_URL} from '../../../environments/api-config';
import {Friend} from '../../models/friend.model';

@Injectable({
  providedIn: 'root'
})
export class FriendshipService {
  private apiUrl = PROXY_API_URL + '/friends';

  constructor(private http : HttpClient) {}

  getFriends(userId : number) : Observable<Friend[]> {
    return this.http.get<Friend[]>(`${this.apiUrl}/list`, {
      params:{
        userId: userId
      }
    });
  }

  deleteFriend(userId: number, friendId: number) : Observable<any>{
    return this.http.delete(`${this.apiUrl}/remove`, {
      params:{
        userId: userId,
        friendId: friendId
      }
    });
  }
}
