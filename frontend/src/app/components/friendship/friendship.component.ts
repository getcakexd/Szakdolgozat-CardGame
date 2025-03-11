import {Component, OnInit} from '@angular/core';
import {FriendshipService} from '../../services/friendship/friendship.service';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-friendship',
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './friendship.component.html',
  standalone: true,
  styleUrl: './friendship.component.css'
})
export class FriendshipComponent implements OnInit{
  friends: any[] = [];
  currentUserId: string | null = localStorage.getItem('id');

  constructor(private friendshipService: FriendshipService) {}

  ngOnInit(): void {
    if (this.currentUserId) {
      this.loadFriends();
    }
  }

  loadFriends(): void {
    if (this.currentUserId) {
      this.friendshipService.getFriends(this.currentUserId).subscribe(
        (data) => {
          this.friends = data
          console.log(this.friends)
        },
        (error) => console.error('Hiba a barátok lekérésekor:', error)
      );
    }
  }

  deleteFriend(friendId: string): void {
    if (this.currentUserId) {
      this.friendshipService.deleteFriend(this.currentUserId, friendId).subscribe(
        () => {
          this.friends = this.friends.filter(friend => friend.id !== friendId);
        },
        (error) => console.error('Hiba a törlés során:', error)
      );
    }
  }
}
