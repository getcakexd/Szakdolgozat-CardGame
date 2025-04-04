import { Component } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {AuthService} from '../../services/auth/auth.service';
import {ConfirmDialogComponent} from '../../components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-profile',
  imports: [
    FormsModule,
    NgIf,
    MatCardModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatProgressBarModule
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
  isLoading: boolean = false;
  hidePassword: boolean = true;
  hideCurrentPassword: boolean = true;
  hideNewPassword: boolean = true;

  private deleteSource = new Subject<void>();
  delete$ = this.deleteSource.asObservable();
  private userId: number;

  constructor(
    protected userService: UserService,
    protected authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.userId = this.authService.getCurrentUserId() || 0;
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']).then();
    }
  }

  toggleEditForm(field: string): void {
    this.editField = field;
    this.editForm = true;
    this.message = '';

    if (field === 'username') {
      this.newUsername = this.authService.currentUser?.username || '';
    } else if (field === 'email') {
      this.newEmail = this.authService.currentUser?.email || '';
    }
  }

  toggleDeleteForm(): void {
    this.deleteFormVisible = !this.deleteFormVisible;
    this.message = '';
    this.password = '';
  }

  updateUser(): void {
    this.isLoading = true;

    if (this.editField === 'username') {
      this.userService.updateUsername(this.userId, this.newUsername).subscribe({
        next: (response) => {
          this.authService.currentUser!.username = this.newUsername;
          this.cancelEdit();
          this.isLoading = false;
          this.snackBar.open('Username updated successfully!', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.message = error.message || 'Failed to update username';
          this.isLoading = false;
          this.snackBar.open(this.message, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
        }
      });
    } else if (this.editField === 'email') {
      this.userService.updateEmail(this.userId, this.newEmail).subscribe({
        next: (response) => {
          this.authService.currentUser!.email = this.newEmail;
          this.cancelEdit();
          this.isLoading = false;
          this.snackBar.open('Email updated successfully!', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.message = error.message || 'Failed to update email';
          this.isLoading = false;
          this.snackBar.open(this.message, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
        }
      });
    } else if (this.editField === 'password') {
      this.userService.updatePassword(this.userId, this.currentPassword, this.newPassword).subscribe({
        next: (response) => {
          this.authService.currentUser!.password = this.newPassword;
          this.cancelEdit();
          this.isLoading = false;
          this.snackBar.open('Password updated successfully!', 'Close', { duration: 3000 });
        },
        error: (error) => {
          this.message = error.message || 'Failed to update password';
          this.isLoading = false;
          this.snackBar.open(this.message, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
        }
      });
    }
  }

  deleteProfile(): void {
    if (!this.password) {
      this.message = 'Password is required to delete your account';
      return;
    }

    this.isLoading = true;

    this.userService.deleteAccount(this.userId, this.password).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open('Profile deleted successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/login']).then(() => {
          window.location.reload();
        });
      },
      error: (error) => {
        this.message = error.message || 'Failed to delete account';
        this.isLoading = false;
        this.snackBar.open(this.message, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
      }
    });
  }

  cancelEdit(): void {
    this.editField = '';
    this.newUsername = '';
    this.newEmail = '';
    this.currentPassword = '';
    this.newPassword = '';
    this.message = '';
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: { title: 'Delete Account', message: 'Are you sure you want to delete your account? This action cannot be undone.' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.toggleDeleteForm();
      }
    });
  }
}


