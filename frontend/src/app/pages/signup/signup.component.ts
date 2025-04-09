import { Component, OnDestroy, OnInit } from '@angular/core';
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
import { Router, RouterLink } from '@angular/router';
import { GoogleSigninButtonModule, SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';
import { AuthService } from '../../services/auth/auth.service';
import { Subscription } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-signup",
  templateUrl: "./signup.component.html",
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
    RouterLink,
    GoogleSigninButtonModule,
    TranslateModule
  ],
  styleUrls: ["./signup.component.css"],
})
export class SignupComponent implements OnInit, OnDestroy {
  signupForm = new FormGroup({
    username: new FormControl("", [Validators.required]),
    email: new FormControl("", [Validators.required, Validators.email]),
    password: new FormControl("", [Validators.required, Validators.minLength(6)]),
  })

  isLoading = false
  message: string | null = null
  isSuccess = false
  hidePassword = true
  socialUser!: SocialUser
  private authStateSubscription: Subscription | null = null

  constructor(
    private userService: UserService,
    protected authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    private socialAuthService: SocialAuthService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.authStateSubscription = this.socialAuthService.authState.subscribe((user) => {
        if (user) {
          this.handleGoogleSignup(user)
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

  private handleGoogleSignup(user: SocialUser): void {
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
        this.message = error?.error?.message || this.translate.instant('ERRORS.GOOGLE_SIGNUP_ERROR')
        if (this.message != null) {
          this.snackBar.open(this.message, this.translate.instant('CLOSE'), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        }
      },
    })
  }

  createUser(): void {
    if (this.signupForm.valid) {
      this.isLoading = true
      const newUser: User = {
        id: 0,
        username: this.signupForm.get("username")?.value!,
        email: this.signupForm.get("email")?.value!,
        password: this.signupForm.get("password")?.value!,
        role: "",
      }

      this.userService.createUser(newUser).subscribe({
        next: (response) => {
          this.message = response.message
          this.isSuccess = true
          this.isLoading = false
          this.resetForm()

          this.snackBar.open(
            this.translate.instant('SUCCESS.ACCOUNT_CREATED'),
            this.translate.instant('CLOSE'),
            {
              duration: 5000,
              panelClass: ["success-snackbar"],
            }
          )

          setTimeout(() => {
            this.router.navigate(["/login"])
          }, 1500)
        },
        error: (error) => {
          this.message = error?.error?.message || this.translate.instant('ERRORS.USER_CREATE_ERROR')
          this.isSuccess = false
          this.isLoading = false

          if (this.message != null) {
            this.snackBar.open(this.message, this.translate.instant('CLOSE'), {
              duration: 5000,
              panelClass: ["error-snackbar"],
            })
          }
        },
      })
    } else {
      this.signupForm.markAllAsTouched()
      this.message = this.translate.instant('ERRORS.FILL_ALL_FIELDS')
      this.isSuccess = false
    }
  }

  getErrorMessage(field: string): string {
    const control = this.signupForm.get(field)
    if (control?.hasError("required")) {
      if (field === 'username') return this.translate.instant('ERRORS.USERNAME_REQUIRED')
      if (field === 'email') return this.translate.instant('ERRORS.EMAIL_REQUIRED')
      if (field === 'password') return this.translate.instant('ERRORS.PASSWORD_REQUIRED')
      return this.translate.instant('ERRORS.REQUIRED_FIELD')
    }
    if (field === "email" && control?.hasError("email")) {
      return this.translate.instant('ERRORS.EMAIL_INVALID')
    }
    if (field === "password" && control?.hasError("minlength")) {
      return this.translate.instant('ERRORS.PASSWORD_MIN_LENGTH')
    }
    return ""
  }

  private resetForm(): void {
    this.signupForm.reset()
    Object.keys(this.signupForm.controls).forEach((key) => {
      this.signupForm.get(key)?.setErrors(null)
    })
  }
}
