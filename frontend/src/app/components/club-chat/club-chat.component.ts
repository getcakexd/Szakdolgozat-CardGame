import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {ClubChatService} from '../../services/club-chat/club-chat.service';
import {ClubMessage} from '../../models/club-message.model';
import {FormsModule} from '@angular/forms';
import {ClubService} from '../../services/club/club.service';
import {Club} from '../../models/club.model';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {ClubMemberService} from '../../services/club-member/club-member.service';

@Component({
  selector: 'app-club-chat',
  imports: [FormsModule, NgForOf, NgClass, DatePipe, NgIf],
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
  senderId = parseInt(localStorage.getItem('id') || '');
  hasPermission:boolean = false;

  constructor(
    private clubChatService: ClubChatService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService
  ) {}

  ngOnInit(): void {
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
    this.clubChatService.getMessages(this.clubId).subscribe((messages) => {
      this.messages = messages;
      this.scrollToBottom();
    });
  }

  setHasPermission() {
    this.clubMemberService.getUserRole(this.clubId, this.senderId).subscribe((role) => {
      this.hasPermission = role.role === 'admin' || role.role === 'moderator';
    });
  }

  sendMessage() {
    this.clubChatService.sendMessage(this.clubId, this.senderId, this.newMessage).subscribe((sentMessage: ClubMessage) => {
      this.messages.push(sentMessage);
      this.newMessage = '';
      this.scrollToBottom();
    });
  }

  unsendMessage(messageId: number) {
    this.clubChatService.unsendMessage(messageId).subscribe(() => {
      this.loadMessages();
    });
  }

  removeMessage(messageId: number) {
    this.clubChatService.removeMessage(messageId).subscribe(() => {
      this.loadMessages();
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
