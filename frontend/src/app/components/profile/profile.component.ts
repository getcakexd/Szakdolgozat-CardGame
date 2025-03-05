import { Component } from '@angular/core';
import {UserService} from '../../services/user.service';
import {FormsModule} from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-profile',
  imports: [
    FormsModule,
    NgIf,
    NgClass
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
      this.userService.updateUsername(this.userService.getLoggedInId(), this.newUsername).subscribe(response => {
        if (response.status === "ok") {
          this.userService.setLoggedInUsername(this.newUsername);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        } else {
          this.message = response.message;
        }
      });
    } else if (this.editField === 'email') {
      this.userService.updateEmail(this.userService.getLoggedInId(), this.newEmail).subscribe(response => {
        if (response.status === "ok") {
          this.userService.setLoggedInEmail(this.newEmail);
          this.cancelEdit();
          this.router.navigate(['/profile']).then();
        } else {
          this.message = response.message;
        }
      });
    } else if (this.editField === 'password') {
      this.userService.updatePassword(this.userService.getLoggedInId(), this.userService.getLoggedInPassword(), this.newPassword).subscribe(response => {
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
    this.userService.deleteAccount(this.userService.getLoggedInId(), this.password).subscribe(response => {
      if (response.status === 'ok') {
        alert('Profile deleted successfully!');
        this.router.navigate(['/login']).then();
      } else {
        this.message = response.message;
      }
    });
  }

  cancelEdit(): void {
    this.editForm = false;
    this.newUsername = '';
    this.newEmail = '';
    this.currentPassword = '';
    this.newPassword = '';

  }
}
