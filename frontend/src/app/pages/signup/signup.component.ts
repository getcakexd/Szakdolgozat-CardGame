import { Component } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import {User} from '../../models/user.model';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatCard} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgIf,
    MatError,
    MatFormField,
    MatCard,
    ReactiveFormsModule,
    MatButton,
    MatInput,
  ],
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  newUsernameFormControl = new FormControl('', [Validators.required]);
  emailFormControl = new FormControl('', [Validators.required, Validators.email]);
  newPasswordFormControl = new FormControl('', [Validators.required, Validators.minLength(6)]);

  isLoading = false;
  message: string | null = null;
  isSuccess = false;

  constructor(private userService: UserService) {}

  createUser(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    const newUser: User = {
      id: 0,
      username: this.newUsernameFormControl.value!,
      email: this.emailFormControl.value!,
      password: this.newPasswordFormControl.value!,
      role: ''
    };

    this.userService.createUser(newUser).subscribe({
      next: (response) => {
        this.message = response.message;
        this.isSuccess = true;
        this.isLoading = false;
        this.resetForm();
      },
      error: (error) => {
        this.message = error?.error?.message || 'Error creating user. Please try again.';
        this.isSuccess = false;
        this.isLoading = false;
        console.error(error);
      }
    });
  }

  private validateForm(): boolean {
    if (!this.newUsernameFormControl.value?.trim()) {
      this.showError('Username is required.');
      return false;
    }
    if (!this.emailFormControl.value?.trim() || !this.isValidEmail(this.emailFormControl.value)) {
      this.showError('A valid email is required.');
      return false;
    }
    if (!this.newPasswordFormControl.value?.trim() || this.newPasswordFormControl.value.length < 6) {
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
    this.newUsernameFormControl.reset();
    this.emailFormControl.reset();
    this.newPasswordFormControl.reset();
  }

  private isValidEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
}
