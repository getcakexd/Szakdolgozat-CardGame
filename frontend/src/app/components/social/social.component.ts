import { Component } from '@angular/core';
import {FriendRequestsComponent} from '../friend-request/friend-request.component';
import {FriendshipComponent} from '../friendship/friendship.component';

@Component({
  selector: 'app-social',
  imports: [
    FriendRequestsComponent,
    FriendshipComponent
  ],
  templateUrl: './social.component.html',
  standalone: true,
  styleUrl: './social.component.css'
})
export class SocialComponent {

}
