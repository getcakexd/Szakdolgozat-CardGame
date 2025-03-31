import { Injectable } from '@angular/core';
import {BACKEND_API_URL} from '../../../environments/api-config';
import {HttpClient} from '@angular/common/http';
import {ClubMessage} from '../../models/club-message.model';

@Injectable({
  providedIn: 'root'
})
export class ClubChatService {
  private apiUrl = BACKEND_API_URL + '/clubchat';

  constructor(private http: HttpClient) { }

  getMessages(clubId: number) {
    return this.http.get<ClubMessage[]>(`${this.apiUrl}/history`,{
        params: {
          clubId: clubId
        }
    });
  }

  sendMessage(clubId: number, senderId: number, content: string) {
    return this.http.post<ClubMessage>(`${this.apiUrl}/send`, null,{
      params: {
        clubId: clubId,
        senderId: senderId,
        content: content
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

  removeMessage(messageId: number) {
    return this.http.put<any[]>(`${this.apiUrl}/remove`, null,{
      params: {
        messageId: messageId
      }
    });
  }
}
