import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PROXY_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private apiUrl = PROXY_API_URL + '/messages';

  constructor(private http : HttpClient) {}

  getMessages(senderId: number, receiverId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/list`, {
      params: new HttpParams().set('userId', senderId.toString()).set('friendId', receiverId.toString()),
    });
  }

  sendMessage(senderId: number, receiverId: number, message: string) {
    return this.http.post<any[]>(`${this.apiUrl}/send`, null,{
      params: new HttpParams().set('userId', senderId.toString()).set('friendId', receiverId.toString()).set('content', message),
    });
  }

  unsendMessage(messageId: number) {
    return this.http.put<any[]>(`${this.apiUrl}/unsend`, null, {
      params: new HttpParams().set('messageId', messageId.toString()),
    });
  }

  getUnreadMessagesPerFriend(userId: number) {
    return this.http.get<any>(`${this.apiUrl}/unread`, {
      params: new HttpParams().set('userId', userId.toString()),
    });
  }

}
