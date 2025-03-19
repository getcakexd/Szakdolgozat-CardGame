import {Component, OnDestroy, OnInit} from '@angular/core';
import {FriendRequestsComponent} from '../friend-request/friend-request.component';
import {ChatComponent} from '../chat/chat.component';
import {FriendListComponent} from '../friend-list/friend-list.component';
import {FriendRequestService} from '../../services/friend-request/friend-request.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-social',
  imports: [
    FriendRequestsComponent,
    ChatComponent,
    FriendListComponent
  ],
  templateUrl: './social.component.html',
  standalone: true,
  styleUrl: './social.component.css'
})
export class SocialComponent implements OnInit, OnDestroy{
  private reloadSubscription: Subscription = new Subscription();

  constructor(private friendRequestService: FriendRequestService) {}

  ngOnInit() {
    this.reloadSubscription = this.friendRequestService.reload$.subscribe(() => {
      this.reloadFriendList();
    });
  }

  ngOnDestroy() {
    if (this.reloadSubscription) {
      this.reloadSubscription.unsubscribe();
    }
  }

  reloadFriendList() {
    window.location.reload();
  }
}
