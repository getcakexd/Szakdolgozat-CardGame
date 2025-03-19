import { Component } from '@angular/core';
import {FriendRequestsComponent} from '../friend-request/friend-request.component';
import {ChatComponent} from '../chat/chat.component';
import {FriendListComponent} from '../friend-list/friend-list.component';

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
export class SocialComponent {

}
