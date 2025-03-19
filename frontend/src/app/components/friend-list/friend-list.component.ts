import { Component, OnInit } from '@angular/core';
import { Friend } from '../../models/friend.model';
import {NgForOf, NgIf} from '@angular/common';
import {ChatComponent} from '../chat/chat.component';
import {FriendshipService} from '../../services/friendship/friendship.service';

@Component({
  selector: 'app-friend-list',
  templateUrl: './friend-list.component.html',
  styleUrls: ['./friend-list.component.css'],
  imports: [
    NgForOf,
    ChatComponent,
    NgIf
  ],
  standalone: true
})
export class FriendListComponent implements OnInit {
  friends: Friend[] = [];
  openChats: { [key: number]: boolean } = {};
  userId :number = parseInt(localStorage.getItem('id') || '');

  constructor(private friendshipService: FriendshipService) {}

  ngOnInit(): void {
    this.loadFriends();
  }

  loadFriends() {
    this.friendshipService.getFriends(this.userId.toString()).subscribe((data: any[]) => {
      this.friends = data;
    });
  }

  deleteFriend(friendId: string) {
    this.friendshipService.deleteFriend(this.userId.toString(), friendId).subscribe(() => {
      this.loadFriends();
    });
  }

  toggleChat(friendId: string) {
    let friendIdNum = parseInt(friendId);
    this.openChats[friendIdNum] = !this.openChats[friendIdNum];
  }

  protected readonly parseInt = parseInt;
}
