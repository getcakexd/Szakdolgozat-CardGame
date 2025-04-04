import { Component } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NgIf } from '@angular/common';
import { User } from '../../models/user.model';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import {AuthService} from '../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    MatFormFieldModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatProgressBarModule
  ],
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required])
  });

  message: string | null = null;
  isLoading = false;
  hidePassword = true;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  login(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      let username = this.loginForm.get('username')?.value!;
      let password = this.loginForm.get('password')?.value!;

      this.authService.login(username, password).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/home']).then(() => {
            window.location.reload();
          });
        },
        error: (error) => {
          this.isLoading = false;
          this.message = error?.error?.message || "Invalid username or password";
          if (this.message != null) {
            this.snackBar.open(this.message, 'Close', {
              duration: 5000,
              panelClass: ['error-snackbar']
            });
          }
        }
      });
    } else {
      this.message = 'Please fill out all fields correctly.';
      this.loginForm.markAllAsTouched();
    }
  }

  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field);
    if (control?.hasError('required')) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`;
    }
    return '';
  }
}
