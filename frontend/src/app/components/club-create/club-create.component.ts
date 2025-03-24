import {Component, EventEmitter, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ClubService} from '../../services/club/club.service';

@Component({
  selector: 'app-club-create',
  templateUrl: './club-create.component.html',
  standalone: true,
  imports: [
    FormsModule
  ],
  styleUrls: ['./club-create.component.css']
})
export class ClubCreateComponent {
  name: string = '';
  description: string = '';
  isPublic: boolean = true;
  userId: number = parseInt(localStorage.getItem('id') || '');
  message: string = '';

  constructor(private clubService: ClubService) {}



  createClub() {
    this.clubService.createClub(this.name, this.description, this.isPublic, this.userId)
      .subscribe({
        next: (response: any) => {
          this.message = 'Club created successfully!';
          this.resetForm();

        },
        error: (err: any) => {
          this.message = 'Something went wrong while creating club';
        }
      });
  }

  resetForm() {
    this.name = '';
    this.description = '';
    this.isPublic = false;
  }
}
