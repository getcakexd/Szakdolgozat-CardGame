import { Component } from '@angular/core';
import {ClubCreateComponent} from '../../components/club-create/club-create.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-club-page',
  imports: [
    ClubCreateComponent,
    NgIf
  ],
  templateUrl: './club-page.component.html',
  standalone: true,
  styleUrl: './club-page.component.css'
})
export class ClubPageComponent {
  showCreateClub = false;

  toggleCreateClub() {
    this.showCreateClub = !this.showCreateClub;
  }
}
