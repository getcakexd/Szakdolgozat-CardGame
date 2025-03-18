import { Component, OnInit } from '@angular/core';
import { FriendshipService } from '../../services/friendship/friendship.service';
import { ChatService } from '../../services/chat/chat.service';
import { Friend } from '../../models/friend.model';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-friend-list',
  templateUrl: './friend-list.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  styleUrl: './friend-list.component.css'
})
export class FriendListComponent implements OnInit {
  friends: Friend[] = [];
  currentUserId: string | null = localStorage.getItem('id');

  constructor(private friendshipService: FriendshipService, private chatService: ChatService) {}

  ngOnInit(): void {
    if (this.currentUserId) {
      this.loadFriends();
    }
  }

  loadFriends(): void {
    if (this.currentUserId) {
      this.friendshipService.getFriends(this.currentUserId).subscribe(
        (data) => this.friends = data,
        (error) => console.error('Error loading friends:', error)
      );
    }
  }

  deleteFriend(friendId: string): void {
    if (this.currentUserId) {
      this.friendshipService.deleteFriend(this.currentUserId, friendId).subscribe(
        () => this.friends = this.friends.filter(friend => friend.id !== friendId),
        (error) => console.error('Error deleting friend:', error)
      );
    }
  }

  openChat(friend: Friend): void {
    this.chatService.setActiveChat(friend.id);
    friend.hasNewMessage = false;
  }
}
