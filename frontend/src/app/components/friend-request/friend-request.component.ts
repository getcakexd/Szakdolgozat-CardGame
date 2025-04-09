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
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

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
    MatSnackBarModule,
    TranslateModule
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
    private snackBar: MatSnackBar,
    private translate: TranslateService
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
        this.snackBar.open(
          this.translate.instant('SOCIAL.FAILED_LOAD_SENT_REQUESTS'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
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
        this.snackBar.open(
          this.translate.instant('SOCIAL.FAILED_LOAD_INCOMING_REQUESTS'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  sendRequest() {
    if (!this.username.trim()) {
      this.errorMessage = this.translate.instant('SOCIAL.USERNAME_EMPTY');
      return;
    }

    this.isSending = true;
    this.errorMessage = '';

    this.friendRequestService.sendFriendRequest(this.currentUserId, this.username).subscribe({
      next: () => {
        this.loadRequests();
        this.username = '';
        this.isSending = false;
        this.snackBar.open(
          this.translate.instant('SOCIAL.REQUEST_SENT'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      },
      error: (error) => {
        this.errorMessage = error.error?.message || this.translate.instant('SOCIAL.FAILED_SEND_REQUEST');
        this.isSending = false;
        this.snackBar.open(
          this.errorMessage,
          this.translate.instant('COMMON.CLOSE'),
          { duration: 5000 }
        );
      }
    });
  }

  cancelRequest(requestId: number) {
    this.friendRequestService.cancelFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open(
          this.translate.instant('SOCIAL.REQUEST_CANCELLED'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      },
      error: (error) => {
        this.snackBar.open(
          this.translate.instant('SOCIAL.FAILED_CANCEL_REQUEST'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  acceptRequest(requestId: number) {
    this.friendRequestService.acceptFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open(
          this.translate.instant('SOCIAL.REQUEST_ACCEPTED'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
        window.location.reload();
      },
      error: (error) => {
        this.snackBar.open(
          this.translate.instant('SOCIAL.FAILED_ACCEPT_REQUEST'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  declineRequest(requestId: number) {
    this.friendRequestService.declineFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests();
        this.snackBar.open(
          this.translate.instant('SOCIAL.REQUEST_DECLINED'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      },
      error: (error) => {
        this.snackBar.open(
          this.translate.instant('SOCIAL.FAILED_DECLINE_REQUEST'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
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
