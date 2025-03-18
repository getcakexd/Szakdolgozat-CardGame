import { Component, OnInit, OnDestroy } from '@angular/core';
import { ChatService } from '../../services/chat/chat.service';
import { Subscription } from 'rxjs';
import { Message } from '../../models/message.model';
import {NgForOf, NgIf} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule
  ],
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  friendId: string | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  private chatSubscription!: Subscription;
  protected userId = localStorage.getItem('id');

  constructor(private chatService: ChatService) {}

  ngOnInit() {
    this.chatSubscription = this.chatService.getActiveChat().subscribe(friendId => {
      if (friendId) {
        this.friendId = friendId;
        this.loadMessageHistory(friendId);
      }
    });

    this.chatService.receiveMessages().subscribe((message) => {
      if (message.senderId === this.friendId) {
        this.messages.push(message);
      }
    });
  }

  loadMessageHistory(friendId: string): void {
    if (this.userId) {
      this.chatService.getMessageHistory(this.userId, friendId).subscribe(
        (data) => this.messages = data
      );
    }
  }

  sendMessage() {
    if (this.newMessage.trim() && this.userId && this.friendId) {
      const message: Message = {
        id: '',
        senderId: this.userId,
        receiverId: this.friendId,
        content: this.newMessage,
        timestamp: new Date().toISOString()
      };
      this.chatService.sendMessage(message);
      this.messages.push(message);
      this.newMessage = '';
    }
  }

  ngOnDestroy() {
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
    }
  }
}
