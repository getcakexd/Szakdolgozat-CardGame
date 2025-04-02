import { Component } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass, NgIf } from '@angular/common';
import { User } from '../../models/user.model';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgIf,
    MatFormFieldModule,
    MatCardModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink
  ],
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  signupForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

  isLoading = false;
  message: string | null = null;
  isSuccess = false;
  hidePassword = true;

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  createUser(): void {
    if (this.signupForm.valid) {
      this.isLoading = true;
      const newUser: User = {
        id: 0,
        username: this.signupForm.get('username')?.value!,
        email: this.signupForm.get('email')?.value!,
        password: this.signupForm.get('password')?.value!,
        role: ''
      };

      this.userService.createUser(newUser).subscribe({
        next: (response) => {
          this.message = response.message;
          this.isSuccess = true;
          this.isLoading = false;
          this.resetForm();

          this.snackBar.open('Account created successfully! You can now log in.', 'Close', {
            duration: 5000,
            panelClass: ['success-snackbar']
          });

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 1500);
        },
        error: (error) => {
          this.message = error?.error?.message || 'Error creating user. Please try again.';
          this.isSuccess = false;
          this.isLoading = false;

          if (this.message != null) {
            this.snackBar.open(this.message, 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        }
      });
    } else {
      this.signupForm.markAllAsTouched();
      this.message = 'Please fill out all fields correctly.';
      this.isSuccess = false;
    }
  }

  getErrorMessage(field: string): string {
    const control = this.signupForm.get(field);
    if (control?.hasError('required')) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    }
    if (field === 'email' && control?.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (field === 'password' && control?.hasError('minlength')) {
      return 'Password must be at least 6 characters long';
    }
    return '';
  }

  private resetForm(): void {
    this.signupForm.reset();
    Object.keys(this.signupForm.controls).forEach(key => {
      this.signupForm.get(key)?.setErrors(null);
    });
  }
}
