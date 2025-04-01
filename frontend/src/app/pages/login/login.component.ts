import {Component} from '@angular/core';
import { UserService } from '../../services/user/user.service';
import {FormControl, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {NgIf} from '@angular/common';
import {User} from '../../models/user.model';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatCard} from '@angular/material/card';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    MatError,
    MatFormField,
    MatCard,
    MatLabel,
    MatInput,
    MatButton,
    ReactiveFormsModule
  ],
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  usernameFormControl = new FormControl('', [Validators.required]);
  passwordFormControl = new FormControl('', [Validators.required]);

  message: string | null = null;
  isSuccess = false;

  constructor(private userService: UserService, private router: Router) {}

  login(): void {
    if (this.usernameFormControl.valid && this.passwordFormControl.valid) {
      const user: User = {
        id: 0,
        username: this.usernameFormControl.value!,
        password: this.passwordFormControl.value!,
        email: '',
        role: ''
      };

      this.userService.login(user).subscribe({
        next: () => {
          this.router.navigate(['/home']).then(() => {
            window.location.reload();
          });
        },
        error: () => {
          this.message = "Invalid username or password";
        }
      });
    } else {
      this.message = 'Please fill out all fields correctly.';
      this.isSuccess = false;
    }
  }

}
