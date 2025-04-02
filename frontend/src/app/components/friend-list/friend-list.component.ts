import { Component, OnInit } from '@angular/core';
import { Friend } from '../../models/friend.model';
import { NgForOf, NgIf } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
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
    MatExpansionModule,
    MatDividerModule,
    MatTooltipModule,
    MatDialogModule,
    ChatComponent,
    MatProgressSpinner
  ],
  standalone: true
})
export class FriendListComponent implements OnInit {
  friends: Friend[] = [];
  unreadCounts: { [key: number]: number } = {};
  openChats: { [key: number]: boolean } = {};
  userId: number = parseInt(localStorage.getItem('id') || '');
  isLoading: boolean = false;

  constructor(
    private friendshipService: FriendshipService,
    private chatService: ChatService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadFriends();
  }

  loadFriends() {
    this.isLoading = true;
    this.friendshipService.getFriends(this.userId).subscribe({
      next: (data: Friend[]) => {
        this.friends = data;
        this.loadUnreadCounts();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading friends:', error);
        this.snackBar.open('Failed to load friends', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  loadUnreadCounts() {
    this.chatService.getUnreadMessagesPerFriend(this.userId).subscribe({
      next: (data) => {
        this.unreadCounts = data;
      },
      error: (error) => {
        console.error('Error loading unread counts:', error);
      }
    });
  }

  deleteFriend(friendId: string) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Remove Friend', message: 'Are you sure you want to remove this friend?' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.friendshipService.deleteFriend(this.userId, parseInt(friendId)).subscribe({
          next: () => {
            this.snackBar.open('Friend removed successfully', 'Close', { duration: 3000 });
            this.loadFriends();
          },
          error: (error) => {
            console.error('Error removing friend:', error);
            this.snackBar.open('Failed to remove friend', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  toggleChat(friendId: string) {
    let friendIdNum = parseInt(friendId);
    this.openChats[friendIdNum] = !this.openChats[friendIdNum];

    if (this.openChats[friendIdNum] && this.unreadCounts[friendIdNum] > 0) {
      this.unreadCounts[friendIdNum] = 0;
    }
  }

  protected readonly parseInt = parseInt;
}

import { Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

@Component({
  selector: 'confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <mat-dialog-content>{{data.message}}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">Confirm</button>
    </mat-dialog-actions>
  `,
  standalone: true,
  imports: [MatDialogModule, MatButtonModule]
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: {title: string, message: string}) {}
}
