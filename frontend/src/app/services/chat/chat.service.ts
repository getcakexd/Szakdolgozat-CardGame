import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BACKEND_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private apiUrl = BACKEND_API_URL + '/messages';

  constructor(private http : HttpClient) {}

  getMessages(senderId: number, receiverId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/list`, {
      params: {
        userId: senderId,
        friendId: receiverId
      }
    });
  }

  sendMessage(senderId: number, receiverId: number, message: string) {
    return this.http.post<any[]>(`${this.apiUrl}/send`, null,{
      params: {
        userId: senderId,
        friendId: receiverId,
        content: message
      }
    });
  }

  unsendMessage(messageId: number) {
    return this.http.put<any[]>(`${this.apiUrl}/unsend`, null, {
      params: {
        messageId: messageId
      }
    });
  }

  getUnreadMessagesPerFriend(userId: number) {
    return this.http.get<any>(`${this.apiUrl}/unread`, {
      params: {
        userId: userId
      }
    });
  }

}
