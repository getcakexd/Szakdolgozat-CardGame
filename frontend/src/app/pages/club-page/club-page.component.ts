import {Component, OnInit} from '@angular/core';
import {ClubCreateComponent} from '../../components/club-create/club-create.component';
import {NgForOf, NgIf} from '@angular/common';
import {ClubListComponent} from '../../components/club-list/club-list.component';
import {Router} from '@angular/router';
import {ClubInviteService} from '../../services/club-invite/club-invite.service';
import {ClubInvite} from '../../models/club-invite.model';
import {UserService} from '../../services/user/user.service';

@Component({
  selector: 'app-club-page',
  imports: [
    ClubCreateComponent,
    NgIf,
    ClubListComponent,
    NgForOf
  ],
  templateUrl: './club-page.component.html',
  standalone: true,
  styleUrl: './club-page.component.css'
})
export class ClubPageComponent implements OnInit{
  showCreateClub = false;
  showJoinClub = false;
  invites: ClubInvite[] = [];
  userId : number;

  constructor(
    private router: Router,
    private userService: UserService,
    private clubInviteService: ClubInviteService,
  ) {
    this.userId = this.userService.getLoggedInId();
  }

  ngOnInit() {
    this.loadInvites();
  }

  toggleCreateClub() {
    this.showCreateClub = !this.showCreateClub;
  }

  toggleJoinClub() {
    this.showJoinClub = !this.showJoinClub;
  }

  goToClub(clubId: number) {
    this.router.navigate(['/club', clubId]);
  }

  loadInvites() {
    this.clubInviteService.getUserInvites(this.userId).subscribe((data) => {
      this.invites = data;
    });
  }

  acceptInvite(id: number) {
    this.clubInviteService.acceptInvite(id).subscribe(() => {
      window.location.reload();
    });
  }

  declineInvite(id: number) {
    this.clubInviteService.declineInvite(id).subscribe(() => {
      window.location.reload();
    });
  }
}
