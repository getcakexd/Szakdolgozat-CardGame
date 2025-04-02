import { Component, OnInit } from '@angular/core';
import { ClubCreateComponent } from '../../components/club-create/club-create.component';
import { NgForOf, NgIf } from '@angular/common';
import { ClubListComponent } from '../../components/club-list/club-list.component';
import { Router } from '@angular/router';
import { ClubInviteService } from '../../services/club-invite/club-invite.service';
import { ClubInvite } from '../../models/club-invite.model';
import { UserService } from '../../services/user/user.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-club-page',
  imports: [
    ClubCreateComponent,
    NgIf,
    ClubListComponent,
    NgForOf,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatExpansionModule,
    MatBadgeModule
  ],
  templateUrl: './club-page.component.html',
  standalone: true,
  styleUrl: './club-page.component.css'
})
export class ClubPageComponent implements OnInit {
  showCreateClub = false;
  showJoinClub = false;
  invites: ClubInvite[] = [];
  userId: number;
  isLoading = true;

  constructor(
    private router: Router,
    private userService: UserService,
    private clubInviteService: ClubInviteService,
    private snackBar: MatSnackBar
  ) {
    this.userId = this.userService.getLoggedInId();
  }

  ngOnInit() {
    this.loadInvites();
  }

  toggleCreateClub() {
    this.showCreateClub = !this.showCreateClub;
    if (this.showCreateClub) {
      this.showJoinClub = false;
    }
  }

  toggleJoinClub() {
    this.showJoinClub = !this.showJoinClub;
    if (this.showJoinClub) {
      this.showCreateClub = false;
    }
  }

  goToClub(clubId: number) {
    this.router.navigate(['/club', clubId]);
  }

  loadInvites() {
    this.isLoading = true;
    this.clubInviteService.getUserInvites(this.userId).subscribe({
      next: (data) => {
        this.invites = data;
        this.isLoading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to load invites', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  acceptInvite(id: number) {
    this.clubInviteService.acceptInvite(id).subscribe({
      next: () => {
        this.snackBar.open('Invite accepted', 'Close', { duration: 3000 });
        window.location.reload();
      },
      error: (error) => {
        this.snackBar.open('Failed to accept invite', 'Close', { duration: 3000 });
      }
    });
  }

  declineInvite(id: number) {
    this.clubInviteService.declineInvite(id).subscribe({
      next: () => {
        this.snackBar.open('Invite declined', 'Close', { duration: 3000 });
        window.location.reload();
      },
      error: (error) => {
        this.snackBar.open('Failed to decline invite', 'Close', { duration: 3000 });
      }
    });
  }
}
