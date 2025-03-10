import { Component } from '@angular/core';
import {FriendRequestsComponent} from '../friend-request/friend-request.component';

@Component({
  selector: 'app-social',
  imports: [
    FriendRequestsComponent
  ],
  templateUrl: './social.component.html',
  standalone: true,
  styleUrl: './social.component.css'
})
export class SocialComponent {

}
