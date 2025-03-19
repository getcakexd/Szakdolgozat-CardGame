import {Component, Input, numberAttribute, OnInit} from '@angular/core';
import { ChatService } from '../../services/chat/chat.service';
import {DatePipe, NgClass, NgForOf} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {UserService} from '../../services/user/user.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
  imports: [
    NgClass,
    NgForOf,
    FormsModule,
    DatePipe
  ],
  standalone: true
})
export class ChatComponent implements OnInit {
  @Input({transform: numberAttribute}) receiverId!: number;
  receiverName = '';
  messages: any[] = [];
  newMessage: string = '';
  senderId = parseInt(localStorage.getItem('id') || '');

  constructor(private chatService: ChatService, private userService : UserService) {}

  ngOnInit(): void {
    this.loadMessages();
    this.userService.getUserById(this.receiverId.toString()).subscribe((user) => {
      this.receiverName = user.username;
    });
  }

  loadMessages() {
    if (!this.receiverId) return;
    this.chatService.getMessages(this.senderId, this.receiverId).subscribe((messages) => {
      this.messages = messages;
    });
  }

  sendMessage() {
    this.chatService.sendMessage(this.senderId, this.receiverId, this.newMessage).subscribe((sentMessage) => {
      this.messages.push(sentMessage);
      this.newMessage = '';
    });
  }
}
