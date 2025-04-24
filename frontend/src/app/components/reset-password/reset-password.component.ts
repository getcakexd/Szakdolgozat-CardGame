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
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { PasswordResetService } from "../../services/password-reset/password-reset.service"

@Component({
  selector: "app-reset-password",
  templateUrl: "./reset-password.component.html",
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
    RouterLink,
    TranslateModule,
  ],
  styleUrls: ["./reset-password.component.css"],
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm = new FormGroup({
    password: new FormControl("", [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl("", [Validators.required]),
  })

  message: string = ""
  isLoading = false
  isSuccess = false
  isTokenValid = false
  hidePassword = true
  hideConfirmPassword = true
  token: string | null = null

  constructor(
    private passwordResetService: PasswordResetService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

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
      },
      error: () => {
        this.isLoading = false
        this.message = this.translate.instant("PASSWORD_RESET.INVALID_OR_EXPIRED_TOKEN")
      },
    })
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
    if (field === "confirmPassword" && !this.passwordsMatch() && control?.touched) {
      return this.translate.instant("ERRORS.PASSWORDS_DONT_MATCH")
    }
    return ""
  }
}
