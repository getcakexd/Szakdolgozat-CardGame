import { Component, OnInit } from '@angular/core';
import {FriendRequestService} from '../../services/friend-request/friend-request.service';
import {FormsModule} from '@angular/forms';
import {NgForOf, NgIf} from '@angular/common';
import {Observable} from 'rxjs';
import {UserService} from '../../services/user/user.service';
import {FriendRequest} from '../../models/FriendRequest';


@Component({
  selector: 'app-friend-request',
  templateUrl: './friend-request.component.html',
  styleUrls: ['./friend-request.component.css'],
  imports: [
    FormsModule,
    NgIf,
    NgForOf
  ],
  standalone: true
})
export class FriendRequestsComponent implements OnInit {
  username: string = '';
  errorMessage: string = '';
  sentRequests: FriendRequest[] = [];
  incomingRequests: FriendRequest[] = [];
  currentUserId: number;

  constructor(
    private userService: UserService,
    private friendRequestService: FriendRequestService
  ) {
    this.currentUserId = this.userService.getLoggedInId();
  }

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests() {
    this.friendRequestService.getSentRequests(this.currentUserId).subscribe((requests) => {
      this.sentRequests = requests;
    });

    this.friendRequestService.getIncomingRequests(this.currentUserId).subscribe((requests) => {
      this.incomingRequests = requests;
    });
  }

  sendRequest() {
    if (!this.username.trim()) {
      this.errorMessage = 'Username cannot be empty.';
      return;
    }

    this.friendRequestService.sendFriendRequest(this.currentUserId, this.username).subscribe(
      (response) => {
        if (response.status === 'ok') {
          this.username = '';
          this.errorMessage = '';
          this.loadRequests();
        } else {
          this.errorMessage = response.message;
        }
      }
    );
  }

  cancelRequest(requestId: number) {
    this.friendRequestService.cancelFriendRequest(requestId).subscribe(() => {
      this.loadRequests();
    });
  }

  acceptRequest(requestId: number) {
    this.friendRequestService.acceptFriendRequest(requestId).subscribe(() => {
      this.loadRequests();
    });
  }

  declineRequest(requestId: number) {
    this.friendRequestService.declineFriendRequest(requestId).subscribe(() => {
      this.loadRequests();
    });
  }
}
