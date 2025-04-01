import {Component, ElementRef, Input, numberAttribute, OnInit, ViewChild} from '@angular/core';
import { ChatService } from '../../services/chat/chat.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user/user.service';
import {MatCard, MatCardContent, MatCardHeader} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
  imports: [
    NgClass,
    NgForOf,
    FormsModule,
    DatePipe,
    NgIf,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatButton,
    MatFormField,
    MatInput,
    MatLabel
  ],
  standalone: true
})
export class ChatComponent implements OnInit {
  @Input({transform: numberAttribute}) receiverId!: number;
  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  receiverName = '';
  messages: any[] = [];
  newMessage: string = '';
  senderId = parseInt(localStorage.getItem('id') || '');

  constructor(private chatService: ChatService, private userService : UserService) {}

  ngOnInit(): void {
    this.loadMessages();
    this.userService.getUserById(this.receiverId).subscribe((user) => {
      this.receiverName = user.username;
    });
  }

  loadMessages() {
    if (!this.receiverId) return;
    this.chatService.getMessages(this.senderId, this.receiverId).subscribe((messages) => {
      this.messages = messages;
      this.scrollToBottom();
    });
  }

  sendMessage() {
    this.chatService.sendMessage(this.senderId, this.receiverId, this.newMessage).subscribe((sentMessage) => {
      this.messages.push(sentMessage);
      this.newMessage = '';
      this.scrollToBottom();
    });
  }

  unsendMessage(messageId: number) {
    this.chatService.unsendMessage(messageId).subscribe(() => {
      this.messages = this.messages.map(msg =>
        msg.id === messageId ? { ...msg, unsent: true } : msg
      );
    });
  }

  private scrollToBottom(): void {
    try {
      setTimeout(() => {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      }, 100);
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }
}
