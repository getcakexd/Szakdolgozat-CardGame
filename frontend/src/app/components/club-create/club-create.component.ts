import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../services/club/club.service';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-club-create',
  templateUrl: './club-create.component.html',
  standalone: true,
  imports: [
    FormsModule,
    MatInputModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatButtonModule,
    MatCardModule,
    NgIf
  ],
  styleUrls: ['./club-create.component.css']
})
export class ClubCreateComponent {
  name: string = '';
  description: string = '';
  isPublic: boolean = true;
  userId: number = parseInt(localStorage.getItem('id') || '');
  message: string = '';
  isSubmitting: boolean = false;

  constructor(
    private clubService: ClubService,
    private snackBar: MatSnackBar
  ) {}

  createClub() {
    if (!this.name.trim()) {
      this.snackBar.open('Club name is required', 'Close', { duration: 3000 });
      return;
    }

    this.isSubmitting = true;
    this.clubService.createClub(this.name, this.description, this.isPublic, this.userId)
      .subscribe({
        next: (response: any) => {
          this.snackBar.open('Club created successfully!', 'Close', { duration: 3000 });
          this.resetForm();
          window.location.reload();
        },
        error: (err: any) => {
          this.message = 'Something went wrong while creating club';
          this.snackBar.open(this.message, 'Close', { duration: 3000 });
          this.isSubmitting = false;
        }
      });
  }

  resetForm() {
    this.name = '';
    this.description = '';
    this.isPublic = true;
    this.isSubmitting = false;
  }
}
