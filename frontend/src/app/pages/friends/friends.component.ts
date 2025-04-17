import { Component } from '@angular/core';
import {Subscription} from 'rxjs';
import {FriendRequestService} from '../../services/friend-request/friend-request.service';
import {FriendListComponent} from '../../components/friend-list/friend-list.component';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatDividerModule} from '@angular/material/divider';
import {MatTabsModule} from '@angular/material/tabs';
import {FriendRequestComponent} from '../../components/friend-request/friend-request.component';
import {TranslateModule} from '@ngx-translate/core';

@Component({
  selector: 'app-friends',
  imports: [
    FriendListComponent,
    MatIconModule,
    MatCardModule,
    MatDividerModule,
    MatTabsModule,
    FriendRequestComponent,
    TranslateModule
  ],
  templateUrl: './friends.component.html',
  standalone: true,
  styleUrl: './friends.component.css'
})
export class FriendsComponent {
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
