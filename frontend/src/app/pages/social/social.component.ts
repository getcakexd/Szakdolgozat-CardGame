import { Component, OnDestroy, OnInit } from '@angular/core';
import { FriendRequestComponent } from '../../components/friend-request/friend-request.component';
import { FriendListComponent } from '../../components/friend-list/friend-list.component';
import { FriendRequestService } from '../../services/friend-request/friend-request.service';
import { Subscription } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-social',
  imports: [
    FriendListComponent,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatTabsModule,
    FriendRequestComponent
  ],
  templateUrl: './social.component.html',
  standalone: true,
  styleUrls: ['./social.component.css']
})
export class SocialComponent implements OnInit, OnDestroy {
  private reloadSubscription: Subscription = new Subscription();
  isLoading: boolean = false;

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
    this.isLoading = true;
    setTimeout(() => {
      window.location.reload();
    }, 500);
  }
}
