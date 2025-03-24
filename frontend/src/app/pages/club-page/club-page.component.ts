import { Component } from '@angular/core';
import {ClubCreateComponent} from '../../components/club-create/club-create.component';
import {NgIf} from '@angular/common';
import {ClubListComponent} from '../../components/club-list/club-list.component';
import {Router} from '@angular/router';

@Component({
  selector: 'app-club-page',
  imports: [
    ClubCreateComponent,
    NgIf,
    ClubListComponent
  ],
  templateUrl: './club-page.component.html',
  standalone: true,
  styleUrl: './club-page.component.css'
})
export class ClubPageComponent {
  showCreateClub = false;
  showJoinClub = false;

  constructor(private router: Router) {}

  toggleCreateClub() {
    this.showCreateClub = !this.showCreateClub;
  }

  toggleJoinClub() {
    this.showJoinClub = !this.showJoinClub;
  }

  goToClub(clubId: number) {
    this.router.navigate(['/club', clubId]);
  }
}
