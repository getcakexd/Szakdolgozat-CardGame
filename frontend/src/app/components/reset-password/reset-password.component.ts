import { Component, OnInit } from "@angular/core"
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms"
import { ActivatedRoute, Router, RouterLink } from "@angular/router"
import { NgIf } from "@angular/common"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatCardModule } from "@angular/material/card"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatSnackBar } from "@angular/material/snack-bar"
import { PasswordResetService } from "../../services/password-reset/password-reset.service"
import { PasswordValidatorService } from "../../services/password-validator/password-validator.service"
import { NgClass } from "@angular/common"
import { TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: "app-reset-password",
  templateUrl: "./reset-password.component.html",
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgClass,
    MatFormFieldModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink,
    TranslatePipe,
  ],
  styleUrls: ["./reset-password.component.scss"],
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm = {} as FormGroup

  message = ""
  isLoading = false
  isSuccess = false
  isTokenValid = false
  hidePassword = true
  hideConfirmPassword = true
  token: string | null = null

  passwordStrengthInfo = {
    minLength: false,
    hasUpperCase: false,
    hasLowerCase: false,
    hasNumeric: false,
    hasSpecialChar: false,
  }

  constructor(
    private passwordResetService: PasswordResetService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private passwordValidator: PasswordValidatorService,
  ) {
    this.resetPasswordForm = new FormGroup({
      password: new FormControl("", [
        Validators.required,
        Validators.minLength(8),
        this.passwordValidator.createPasswordStrengthValidator(),
      ]),
      confirmPassword: new FormControl("", [Validators.required]),
    })
  }

  ngOnInit(): void {
    this.isLoading = true
    this.token = this.route.snapshot.queryParamMap.get("token")

    if (!this.token) {
      this.isLoading = false
      this.message = this.translate.instant("PASSWORD_RESET.INVALID_TOKEN")
      return
    }

    this.passwordResetService.validateResetToken(this.token).subscribe({
      next: () => {
        this.isLoading = false
        this.isTokenValid = true

        this.resetPasswordForm.get("password")?.valueChanges.subscribe((value) => {
          this.updatePasswordStrengthInfo(value)
        })
      },
      error: () => {
        this.isLoading = false
        this.message = this.translate.instant("PASSWORD_RESET.INVALID_OR_EXPIRED_TOKEN")
      },
    })
  }

  private updatePasswordStrengthInfo(password: string): void {
    if (!password) {
      this.resetPasswordStrengthInfo()
      return
    }

    this.passwordStrengthInfo = {
      minLength: password.length >= 8,
      hasUpperCase: /[A-Z]/.test(password),
      hasLowerCase: /[a-z]/.test(password),
      hasNumeric: /[0-9]/.test(password),
      hasSpecialChar: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password),
    }
  }

  private resetPasswordStrengthInfo(): void {
    this.passwordStrengthInfo = {
      minLength: false,
      hasUpperCase: false,
      hasLowerCase: false,
      hasNumeric: false,
      hasSpecialChar: false,
    }
  }

  resetPassword(): void {
    if (this.resetPasswordForm.valid && this.passwordsMatch()) {
      this.isLoading = true
      const newPassword = this.resetPasswordForm.get("password")?.value!

      this.passwordResetService.resetPassword(this.token!, newPassword).subscribe({
        next: () => {
          this.isLoading = false
          this.isSuccess = true
          this.message = this.translate.instant("PASSWORD_RESET.SUCCESS")
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["success-snackbar"],
          })
        },
        error: (error) => {
          this.isLoading = false
          this.message = error?.error?.message || this.translate.instant("PASSWORD_RESET.ERROR")
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        },
      })
    } else {
      this.resetPasswordForm.markAllAsTouched()
    }
  }

  passwordsMatch(): boolean {
    const password = this.resetPasswordForm.get("password")?.value
    const confirmPassword = this.resetPasswordForm.get("confirmPassword")?.value
    return password === confirmPassword
  }

  getErrorMessage(field: string): string {
    const control = this.resetPasswordForm.get(field)
    if (control?.hasError("required")) {
      return this.translate.instant("ERRORS.PASSWORD_REQUIRED")
    }
    if (control?.hasError("minlength")) {
      return this.translate.instant("ERRORS.PASSWORD_TOO_SHORT")
    }
    if (field === "password" && control?.hasError("passwordStrength")) {
      return this.passwordValidator.getPasswordErrorMessage(control.errors)
    }
    if (field === "confirmPassword" && !this.passwordsMatch() && control?.touched) {
      return this.translate.instant("ERRORS.PASSWORDS_DONT_MATCH")
    }
    return ""
  }
}
