import {Component, OnDestroy, OnInit} from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { NgIf } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import {AuthService} from '../../services/auth/auth.service';
import {GoogleSigninButtonModule, SocialAuthService, SocialUser} from '@abacritt/angularx-social-login';
import {Subscription} from 'rxjs';


@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
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
    MatProgressBarModule,
    GoogleSigninButtonModule,
    RouterLink,
  ],
  styleUrls: ["./login.component.css"],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm = new FormGroup({
    username: new FormControl("", [Validators.required]),
    password: new FormControl("", [Validators.required]),
  })

  message: string | null = null
  isLoading = false
  hidePassword = true
  socialUser!: SocialUser
  private authStateSubscription: Subscription | null = null

  constructor(
    private userService: UserService,
    protected authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private socialAuthService: SocialAuthService,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.authStateSubscription = this.socialAuthService.authState.subscribe((user) => {
        if (user) {
          this.handleGoogleLogin(user)
        }
      })
    } else {
      this.router.navigate(["/home"])
    }
  }

  ngOnDestroy(): void {
    if (this.authStateSubscription) {
      this.authStateSubscription.unsubscribe()
    }
  }

  private handleGoogleLogin(user: SocialUser): void {
    if (this.isLoading) return

    this.isLoading = true
    this.authService.loginWithGoogle(user).subscribe({
      next: () => {
        this.isLoading = false
        this.router.navigate(["/home"]).then(() => {
          window.location.reload()
        })
      },
      error: (error) => {
        this.isLoading = false
        this.message = error?.error?.message || "Error logging in with Google"
        if (this.message != null) {
          this.snackBar.open(this.message, "Close", {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        }
      },
    })
  }

  login(): void {
    if (this.loginForm.valid) {
      this.isLoading = true
      const username = this.loginForm.get("username")?.value!
      const password = this.loginForm.get("password")?.value!

      this.authService.login(username, password).subscribe({
        next: () => {
          this.isLoading = false
          this.router.navigate(["/home"]).then(() => {
            window.location.reload()
          })
        },
        error: (error) => {
          this.isLoading = false
          this.message = error?.error?.message || "Invalid username or password"
          if (this.message != null) {
            this.snackBar.open(this.message, "Close", {
              duration: 5000,
              panelClass: ["error-snackbar"],
            })
          }
        },
      })
    } else {
      this.message = "Please fill out all fields correctly."
      this.loginForm.markAllAsTouched()
    }
  }

  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field)
    if (control?.hasError("required")) {
      return `${field.charAt(0).toUpperCase() + field.slice(1)} is required`
    }
    return ""
  }
}
