import { Component } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-profile',
  imports: [
    FormsModule,
    NgIf,
    MatCard,
    MatCardTitle,
    MatLabel,

    MatCardHeader,
    MatCardContent,
    MatFormField,
    MatButton,
    MatInput,
  ],
  templateUrl: './profile.component.html',
  standalone: true,
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent {
  editForm: boolean = false;
  editField: string = '';
  newUsername: string = '';
  newEmail: string = '';
  currentPassword: string = '';
  newPassword: string = '';
  deleteFormVisible: boolean = false;
  message: string = '';
  password: string = '';
  private deleteSource = new Subject<void>();
  delete$ = this.deleteSource.asObservable();
  private userId: number;

  constructor(
    protected userService: UserService, private router: Router
  ) {
    this.userId = this.userService.getLoggedInId();
    if (!this.userService.isLoggedIn()) {
      this.router.navigate(['/login']).then();
    }
  }

  toggleEditForm(field: string): void {
    this.editField = field;
    this.editForm = true;

    if (field === 'username') {
      this.newUsername = this.userService.getLoggedInUsername();
    } else if (field === 'email') {
      this.newEmail = this.userService.getLoggedInEmail();
    }
  }

  toggleDeleteForm(): void {
    this.deleteFormVisible = !this.deleteFormVisible;
  }

  updateUser(): void {
    if (this.editField === 'username') {
      this.userService.updateUsername(this.userId, this.newUsername).subscribe({
        next: (response) => {
          this.userService.setLoggedInUsername(this.newUsername);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        },
        error: (error) => {
          this.message = error.message;
        }
      });
    } else if (this.editField === 'email') {
      this.userService.updateEmail(this.userId, this.newEmail).subscribe({
        next: (response) => {
          this.userService.setLoggedInEmail(this.newEmail);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        },
        error: (error) => {
          this.message = error.message;
        }
      });
    } else if (this.editField === 'password') {
      this.userService.updatePassword(this.userId, this.userService.getLoggedInPassword(), this.newPassword).subscribe({
        next: (response) => {
          this.userService.setLoggedInPassword(this.newPassword);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        },
        error: (error) => {
          this.message = error.message;
        }
      });
    }
  }

  deleteProfile(): void {
    this.userService.deleteAccount(this.userId, this.password).subscribe({
      next: (response) => {
        alert('Profile deleted successfully!');
        this.router.navigate(['/login']).then(
          () => {
            window.location.reload();
          }
        );
      },
      error: (error) => {
        this.message = error.message;
      }
    });
  }

  cancelEdit(): void {
    this.editField = '';
    this.newUsername = '';
    this.newEmail = '';
    this.currentPassword = '';
    this.newPassword = '';
  }
}
