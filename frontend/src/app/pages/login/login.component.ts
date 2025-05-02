import { Component, OnDestroy, OnInit } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth/auth.service';
import { GoogleSigninButtonModule, SocialAuthService, SocialUser } from '@abacritt/angularx-social-login';
import { Subscription } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

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
    TranslateModule
  ],
  styleUrls: ["./login.component.css"],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm = new FormGroup({
    email: new FormControl("", [Validators.required, Validators.email]),
    password: new FormControl("", [Validators.required]),
  })

  message: string | null = null
  isLoading = false
  hidePassword = true
  socialUser!: SocialUser
  private authStateSubscription: Subscription | null = null
  unverifiedAccount = false;
  userId: number = 0;
  isSuccess = false;

  constructor(
    private userService: UserService,
    protected authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private socialAuthService: SocialAuthService,
    private translate: TranslateService
  ) {
    this.userId = authService.currentUser?.id || 0
  }

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
        this.message = error?.error?.message || this.translate.instant('ERRORS.GOOGLE_LOGIN_ERROR')
        if (this.message != null) {
          this.snackBar.open(this.message, this.translate.instant('CLOSE'), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        }
      },
    })
  }

  login(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.authService.login(
        this.loginForm.get('email')?.value!,
        this.loginForm.get('password')?.value!
      ).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response && response.unverified) {
            this.unverifiedAccount = true;
            this.userId = response.userId;
            this.message = response.message;
            this.isSuccess = false;
          } else {
            this.router.navigate(['/home']);
            window.location.reload();
          }
        },
        error: (error) => {
          this.isLoading = false;
          if (error?.error?.unverified) {
            this.unverifiedAccount = true;
            this.userId = error.error.userId;
            this.message = error.error.message;
          } else {
            this.message = error?.error || this.translate.instant('ERRORS.INVALID_CREDENTIALS');
          }
          this.isSuccess = false;
        }
      });
    } else {
      this.loginForm.markAllAsTouched();
      this.message = this.translate.instant('ERRORS.FILL_ALL_FIELDS');
      this.isSuccess = false;
    }
  }

  resendVerificationEmail(): void {
    if (!this.userId) return;

    this.isLoading = true;
    this.authService.resendVerificationEmail(this.userId).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.snackBar.open(
          this.translate.instant('EMAIL_VERIFICATION.RESENT'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 5000, panelClass: ['success-snackbar'] }
        );
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error?.error?.message || this.translate.instant('EMAIL_VERIFICATION.RESEND_ERROR');
        this.snackBar.open(
          errorMessage,
          this.translate.instant('COMMON.CLOSE'),
          { duration: 5000, panelClass: ['error-snackbar'] }
        );
      }
    });
  }

  getErrorMessage(field: string): string {
    const control = this.loginForm.get(field)
    if (control?.hasError("required")) {
      return field === 'email'
        ? this.translate.instant('ERRORS.EMAIL_REQUIRED')
        : this.translate.instant('ERRORS.PASSWORD_REQUIRED')
    }
    return ""
  }
}
