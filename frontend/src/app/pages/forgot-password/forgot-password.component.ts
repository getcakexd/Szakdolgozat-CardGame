import { Component, OnInit } from "@angular/core"
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms"
import { Router, RouterLink } from "@angular/router"
import { NgIf } from "@angular/common"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatCardModule } from "@angular/material/card"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatSnackBar } from "@angular/material/snack-bar"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { PasswordResetService } from '../../services/password-reset/password-reset.service';

@Component({
  selector: "app-forgot-password",
  templateUrl: "./forgot-password.component.html",
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
  styleUrls: ["./forgot-password.component.scss"],
})
export class ForgotPasswordComponent implements OnInit {
  forgotPasswordForm = new FormGroup({
    email: new FormControl("", [Validators.required, Validators.email]),
  })

  message: string = ""
  isLoading = false
  isSuccess = false

  constructor(
    private passwordResetService: PasswordResetService,
    private router: Router,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {}

  submitRequest(): void {
    if (this.forgotPasswordForm.valid) {
      this.isLoading = true
      const email = this.forgotPasswordForm.get("email")?.value!

      this.passwordResetService.requestPasswordReset(email).subscribe({
        next: () => {
          this.isLoading = false
          this.isSuccess = true
          this.message = this.translate.instant("PASSWORD_RESET.EMAIL_SENT")
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["success-snackbar"],
          })
        },
        error: (error) => {
          this.isLoading = false
          this.message = error?.error?.message || this.translate.instant("ERRORS.EMAIL_NOT_FOUND")
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        },
      })
    } else {
      this.forgotPasswordForm.markAllAsTouched()
    }
  }

  getErrorMessage(field: string): string {
    const control = this.forgotPasswordForm.get(field)
    if (control?.hasError("required")) {
      return this.translate.instant("ERRORS.EMAIL_REQUIRED")
    }
    if (control?.hasError("email")) {
      return this.translate.instant("ERRORS.INVALID_EMAIL")
    }
    return ""
  }
}
