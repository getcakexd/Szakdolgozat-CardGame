import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { WebSocketSubject } from 'rxjs/webSocket';
import { Message } from '../../models/message.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private socket$: WebSocketSubject<Message>;
  private activeChat$ = new BehaviorSubject<string | null>(null);
  private serverUrl = 'ws://localhost:4200/ws/chat';
  private apiUrl = 'http://localhost:4200/api/messages';

  constructor(private http: HttpClient) {
    this.socket$ = new WebSocketSubject(this.serverUrl);
  }

  setActiveChat(friendId: string): void {
    this.activeChat$.next(friendId);
  }

  getActiveChat(): Observable<string | null> {
    return this.activeChat$.asObservable();
  }

  sendMessage(message: Message): void {
    console.log('Sending message:', message);
    this.socket$.next(message);
    this.http.post<Message>(`${this.apiUrl}/send`, message).subscribe();
  }

  receiveMessages(): Observable<Message> {
    return this.socket$;
  }

  getMessageHistory(userId: string, friendId: string): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/${userId}/${friendId}`);
  }
}
