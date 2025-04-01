import { Component, OnInit } from '@angular/core';
import { Friend } from '../../models/friend.model';
import { NgForOf, NgIf } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { ChatComponent } from '../chat/chat.component';
import { FriendshipService } from '../../services/friendship/friendship.service';
import { ChatService } from '../../services/chat/chat.service';

@Component({
  selector: 'app-friend-list',
  templateUrl: './friend-list.component.html',
  styleUrls: ['./friend-list.component.css'],
  imports: [
    NgForOf,
    NgIf,
    MatListModule,
    MatButtonModule,
    MatBadgeModule,
    MatIconModule,
    MatCardModule,
    ChatComponent
  ],
  standalone: true
})
export class FriendListComponent implements OnInit {
  friends: Friend[] = [];
  unreadCounts: { [key: number]: number } = {};
  openChats: { [key: number]: boolean } = {};
  userId: number = parseInt(localStorage.getItem('id') || '');

  constructor(
    private friendshipService: FriendshipService,
    private chatService: ChatService
  ) {}

  ngOnInit(): void {
    this.loadFriends();
  }

  loadFriends() {
    this.friendshipService.getFriends(this.userId).subscribe((data: Friend[]) => {
      this.friends = data;
      this.loadUnreadCounts();
    });
  }

  loadUnreadCounts() {
    this.chatService.getUnreadMessagesPerFriend(this.userId).subscribe((data) => {
      this.unreadCounts = data;
    });
  }

  deleteFriend(friendId: string) {
    this.friendshipService.deleteFriend(this.userId, parseInt(friendId)).subscribe(() => {
      this.loadFriends();
    });
  }

  toggleChat(friendId: string) {
    let friendIdNum = parseInt(friendId);
    this.openChats[friendIdNum] = !this.openChats[friendIdNum];
  }

  protected readonly parseInt = parseInt;
}
