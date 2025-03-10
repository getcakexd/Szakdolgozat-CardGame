import { Component } from '@angular/core';
import { UserService, User } from '../../services/user/user.service';
import {FormsModule} from '@angular/forms';
import {NgClass, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  newUser: User = { username: '', email: '', password: ''};
  message: string = '';
  isSuccess: boolean = false;
  isLoading: boolean = false;

  constructor(private userService: UserService) {}

  createUser(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.userService.createUser(this.newUser).subscribe(
      (response) => {
        this.message = response.message;
        this.isSuccess = true;
        this.isLoading = false;
        this.resetForm();
      },
      (error) => {
        this.message = error?.error?.message || 'Error creating user. Please try again.';
        this.isSuccess = false;
        this.isLoading = false;
        console.error(error);
      }
    );
  }

  private validateForm(): boolean {
    if (!this.newUser.username.trim()) {
      this.showError('Username is required.');
      return false;
    }
    if (!this.newUser.email.trim() || !this.isValidEmail(this.newUser.email)) {
      this.showError('A valid email is required.');
      return false;
    }
    if (!this.newUser.password.trim() || this.newUser.password.length < 6) {
      this.showError('Password must be at least 6 characters long.');
      return false;
    }
    return true;
  }

  private showError(message: string): void {
    this.message = message;
    this.isSuccess = false;
  }

  private resetForm(): void {
    this.newUser = {username: '', email: '', password: ''};
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
