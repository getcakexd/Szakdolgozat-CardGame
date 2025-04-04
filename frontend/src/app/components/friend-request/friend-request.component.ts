import { Component, OnInit } from '@angular/core';
import { FriendRequestService } from '../../services/friend-request/friend-request.service';
import { FormsModule } from '@angular/forms';
import { NgForOf, NgIf } from '@angular/common';
import { UserService } from '../../services/user/user.service';
import { FriendRequest } from '../../models/FriendRequest';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import {AuthService} from '../../services/auth/auth.service';

@Component({
  selector: 'app-friend-request',
  templateUrl: './friend-request.component.html',
  styleUrls: ['./friend-request.component.css'],
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    MatCardModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  standalone: true
})
export class FriendRequestComponent implements OnInit {
  username: string = '';
  errorMessage: string = '';
  sentRequests: FriendRequest[] = [];
  incomingRequests: FriendRequest[] = [];
  currentUserId: number;
  isLoading: boolean = false;
  isSending: boolean = false;

  constructor(
    private authService: AuthService,
    private friendRequestService: FriendRequestService,
    private snackBar: MatSnackBar
  ) {
    this.currentUserId = this.authService.getCurrentUserId() || 0;
  }

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests() {
    this.isLoading = true;

    this.friendRequestService.getSentRequests(this.currentUserId).subscribe({
      next: (requests) => {
        this.sentRequests = requests;
        this.checkIncomingRequests();
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load sent requests', 'Close', { duration: 3000 });
      }
    });
  }

  checkIncomingRequests() {
    this.friendRequestService.getIncomingRequests(this.currentUserId).subscribe({
      next: (requests) => {
        this.incomingRequests = requests;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open('Failed to load incoming requests', 'Close', { duration: 3000 });
      }
    });
  }

  sendRequest() {
    if (!this.username.trim()) {
      this.errorMessage = 'Username cannot be empty.';
      return;
    }

    this.isSending = true;
    this.errorMessage = '';

    this.friendRequestService.sendFriendRequest(this.currentUserId, this.username).subscribe({
      next: () => {
        this.loadRequests();
        this.username = '';
        this.isSending = false;
        this.snackBar.open('Friend request sent successfully!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to send friend request';
        this.isSending = false;
        this.snackBar.open(this.errorMessage, 'Close', { duration: 5000 });
      }
    });
  }

  cancelRequest(requestId: number) {
    this.friendRequestService.cancelFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open('Friend request cancelled', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open('Failed to cancel request', 'Close', { duration: 3000 });
      }
    });
  }

  acceptRequest(requestId: number) {
    this.friendRequestService.acceptFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open('Friend request accepted!', 'Close', { duration: 3000 });
        window.location.reload();
      },
      error: (error) => {
        this.snackBar.open('Failed to accept request', 'Close', { duration: 3000 });
      }
    });
  }

  declineRequest(requestId: number) {
    this.friendRequestService.declineFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open('Friend request declined', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open('Failed to decline request', 'Close', { duration: 3000 });
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'accepted': return 'primary';
      case 'pending': return 'accent';
      case 'declined': return 'warn';
      default: return '';
    }
  }
}
