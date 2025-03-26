import { Component } from '@angular/core';
import {UserService} from '../../services/user/user.service';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [
    FormsModule,
    NgIf,
  ],
  templateUrl: './profile.component.html',
  standalone: true,
  styleUrl: './profile.component.css'
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
  private userId = parseInt(localStorage.getItem('id') || '0');

  constructor(protected userService: UserService, private router: Router) {
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
      this.userService.updateUsername(this.userId, this.newUsername).subscribe(response => {
        if (response.status === "ok") {
          this.userService.setLoggedInUsername(this.newUsername);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        } else {
          this.message = response.message;
        }
      });
    } else if (this.editField === 'email') {
      this.userService.updateEmail(this.userId, this.newEmail).subscribe(response => {
        if (response.status === "ok") {
          this.userService.setLoggedInEmail(this.newEmail);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        } else {
          this.message = response.message;
        }
      });
    } else if (this.editField === 'password') {
      this.userService.updatePassword(this.userId, this.userService.getLoggedInPassword(), this.newPassword).subscribe(response => {
        if (response.status === "ok") {
          this.userService.setLoggedInPassword(this.newPassword);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        } else {
          this.message = response.message;
        }
      });
    }
  }

  deleteProfile(): void {
    this.userService.deleteAccount(this.userId, this.password).subscribe(response => {
      if (response.status === 'ok') {
        alert('Profile deleted successfully!');
        this.router.navigate(['/login']).then(
          () => {
            window.location.reload();
          }
        );
      } else {
        this.message = response.message;
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
