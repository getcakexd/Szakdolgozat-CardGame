import { Component, ElementRef, Input, numberAttribute, OnInit, ViewChild } from '@angular/core';
import { ChatService } from '../../services/chat/chat.service';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user/user.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

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
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  standalone: true
})
export class ChatComponent implements OnInit {
  @Input({transform: numberAttribute}) receiverId!: number;
  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  receiverName = '';
  messages: any[] = [];
  newMessage: string = '';
  senderId = 0;
  isLoading: boolean = false;

  constructor(
    private chatService: ChatService,
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.senderId = this.authService.getCurrentUserId() || 0;
  }

  ngOnInit(): void {
    this.loadMessages();
    this.loadReceiverInfo();
  }

  loadReceiverInfo() {
    this.userService.getUserById(this.receiverId).subscribe({
      next: (user) => {
        this.receiverName = user.username;
      },
      error: (error) => {
        console.error('Error loading user info:', error);
      }
    });
  }

  loadMessages() {
    if (!this.receiverId) return;

    this.isLoading = true;
    this.chatService.getMessages(this.senderId, this.receiverId).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.scrollToBottom();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoading = false;
        this.snackBar.open(
          this.translate.instant('CHAT.FAILED_LOAD'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    this.chatService.sendMessage(this.senderId, this.receiverId, this.newMessage).subscribe({
      next: (sentMessage) => {
        this.messages.push(sentMessage);
        this.newMessage = '';
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.snackBar.open(
          this.translate.instant('CHAT.FAILED_SEND'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  unsendMessage(messageId: number) {
    this.chatService.unsendMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.map(msg =>
          msg.id === messageId ? { ...msg, unsent: true } : msg
        );
      },
      error: (error) => {
        console.error('Error unsending message:', error);
        this.snackBar.open(
          this.translate.instant('CHAT.FAILED_UNSEND'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
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

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }
}
