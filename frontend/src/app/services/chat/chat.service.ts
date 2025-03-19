import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private apiUrl = 'http://localhost:4200/api/messages';

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

}
