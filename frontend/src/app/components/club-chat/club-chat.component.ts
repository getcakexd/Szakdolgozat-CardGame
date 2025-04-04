import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { ClubChatService } from '../../services/club-chat/club-chat.service';
import { ClubMessage } from '../../models/club-message.model';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../services/club/club.service';
import { Club } from '../../models/club.model';
import { DatePipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { ClubMemberService } from '../../services/club-member/club-member.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {AuthService} from '../../services/auth/auth.service';

@Component({
  selector: 'app-club-chat',
  imports: [
    FormsModule,
    NgForOf,
    NgClass,
    DatePipe,
    NgIf,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDividerModule,
    MatTooltipModule,
    MatBadgeModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './club-chat.component.html',
  standalone: true,
  styleUrls: ['./club-chat.component.css']
})
export class ClubChatComponent implements OnInit {
  @Input() clubId!: number;
  @Input() clubName!: string;
  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  club: any;
  messages: ClubMessage[] = [];
  newMessage: string = '';
  senderId = 0;
  hasPermission: boolean = false;
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private clubChatService: ClubChatService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.getCurrentUserId() || 0;
    this.getClub(this.clubId);
    this.loadMessages();
    this.setHasPermission();
  }

  getClub(clubId: number) {
    this.clubService.getClub(clubId).subscribe((club: Club) => {
      this.club = club;
    });
  }

  loadMessages() {
    if (!this.clubId) return;

    this.isLoading = true;
    this.clubChatService.getMessages(this.clubId).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.scrollToBottom();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.isLoading = false;
      }
    });
  }

  setHasPermission() {
    this.clubMemberService.getUserRole(this.clubId, this.senderId).subscribe((role) => {
      this.hasPermission = role.role === 'admin' || role.role === 'moderator';
    });
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    this.clubChatService.sendMessage(this.clubId, this.senderId, this.newMessage).subscribe({
      next: (sentMessage: ClubMessage) => {
        this.messages.push(sentMessage);
        this.newMessage = '';
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error sending message:', error);
      }
    });
  }

  unsendMessage(messageId: number) {
    this.clubChatService.unsendMessage(messageId).subscribe({
      next: () => {
        this.loadMessages();
      },
      error: (error) => {
        console.error('Error unsending message:', error);
      }
    });
  }

  removeMessage(messageId: number) {
    this.clubChatService.removeMessage(messageId).subscribe({
      next: () => {
        this.loadMessages();
      },
      error: (error) => {
        console.error('Error removing message:', error);
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
