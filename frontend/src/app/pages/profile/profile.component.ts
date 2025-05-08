import { Component, OnInit } from "@angular/core"
import { UserService } from "../../services/user/user.service"
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms"
import { NgIf, NgFor, NgClass } from "@angular/common"
import { Router } from "@angular/router"
import { Subject } from "rxjs"
import { MatCardModule } from "@angular/material/card"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatButtonModule } from "@angular/material/button"
import { MatInputModule } from "@angular/material/input"
import { MatIconModule } from "@angular/material/icon"
import { MatDividerModule } from "@angular/material/divider"
import { MatSnackBar } from "@angular/material/snack-bar"
import { MatDialogModule, MatDialog } from "@angular/material/dialog"
import { MatProgressBarModule } from "@angular/material/progress-bar"
import { MatChipsModule } from "@angular/material/chips"
import { AuthService } from "../../services/auth/auth.service"

import { ConfirmDialogComponent } from "../../components/confirm-dialog/confirm-dialog.component"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { ThemeService } from "../../services/theme/theme.service"
import { PasswordValidatorService } from "../../services/password-validator/password-validator.service"
import {IS_DEV} from '../../../environments/api-config';

interface ThemePalette {
  name: string
  value: string
  displayName: string
}

@Component({
  selector: "app-profile",
  imports: [
    FormsModule,
    NgIf,
    NgFor,
    NgClass,
    MatCardModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatProgressBarModule,
    MatChipsModule,
    TranslateModule,
    ReactiveFormsModule,
  ],
  templateUrl: "./profile.component.html",
  standalone: true,
  styleUrls: ["./profile.component.scss"],
})
export class ProfileComponent implements OnInit {
  editForm = false
  editField = ""
  newUsername = ""
  newEmail = ""
  currentPassword = ""
  newPassword = ""
  deleteFormVisible = false
  message = ""
  password = ""
  isLoading = false
  hidePassword = true
  hideCurrentPassword = true
  hideNewPassword = true
  isGoogleAuthUser = false

  passwordForm = {} as FormGroup

  passwordStrengthInfo = {
    minLength: false,
    hasUpperCase: false,
    hasLowerCase: false,
    hasNumeric: false,
    hasSpecialChar: false,
  }

  availablePalettes: ThemePalette[] = [
    { name: "red", value: "red-palette", displayName: "Red" },
    { name: "green", value: "green-palette", displayName: "Green" },
    { name: "blue", value: "blue-palette", displayName: "Blue" },
    { name: "yellow", value: "yellow-palette", displayName: "Yellow" },
    { name: "cyan", value: "cyan-palette", displayName: "Cyan" },
    { name: "magenta", value: "magenta-palette", displayName: "Magenta" },
    { name: "orange", value: "orange-palette", displayName: "Orange" },
    { name: "chartreuse", value: "chartreuse-palette", displayName: "Chartreuse" },
    { name: "spring-green", value: "spring-green-palette", displayName: "Spring Green" },
    { name: "azure", value: "azure-palette", displayName: "Azure" },
    { name: "violet", value: "violet-palette", displayName: "Violet" },
    { name: "rose", value: "rose-palette", displayName: "Rose" },
  ]

  selectedPalette = ""

  private deleteSource = new Subject<void>()
  delete$ = this.deleteSource.asObservable()
  private userId: number

  constructor(
    protected userService: UserService,
    protected authService: AuthService,
    private themeService: ThemeService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private translate: TranslateService,
    private passwordValidator: PasswordValidatorService,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(["/login"]).then()
    }

    this.passwordForm = new FormGroup({
      currentPassword: new FormControl("", [Validators.required]),
      newPassword: new FormControl("", [
        Validators.required,
        Validators.minLength(8),
        this.passwordValidator.createPasswordStrengthValidator(),
      ]),
    })
  }

  ngOnInit(): void {
    if (this.userId) {
      this.isLoading = true
      this.userService.hasGoogleAuthPassword(this.userId).subscribe({
        next: (response) => {
          this.isGoogleAuthUser = response.hasGoogleAuthPassword
          this.isLoading = false
        },
        error: (error) => {
          if(IS_DEV) console.error("Error checking Google auth status:", error)
          this.isLoading = false
        },
      })

      this.selectedPalette = localStorage.getItem("themePalette") || "blue-palette"

      this.passwordForm.get("newPassword")?.valueChanges.subscribe((value) => {
        this.updatePasswordStrengthInfo(value)
        this.newPassword = value || ""
      })
    }
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

  toggleEditForm(field: string): void {
    this.editField = field
    this.editForm = true
    this.message = ""

    if (field === "username") {
      this.newUsername = this.authService.currentUser?.username || ""
    } else if (field === "email") {
      this.newEmail = this.authService.currentUser?.email || ""
    } else if (field === "password") {
      this.currentPassword = ""
      this.newPassword = ""
      this.passwordForm.reset()
      this.resetPasswordStrengthInfo()
    }
  }

  toggleDeleteForm(): void {
    this.deleteFormVisible = !this.deleteFormVisible
    this.message = ""
    this.password = ""
  }

  updateUser(): void {
    this.isLoading = true

    if (this.editField === "username") {
      this.userService.updateUsername(this.userId, this.newUsername).subscribe({
        next: (response) => {
          this.authService.currentUser!.username = this.newUsername
          this.cancelEdit()
          this.isLoading = false
          this.snackBar.open(this.translate.instant("SUCCESS.USERNAME_UPDATED"), this.translate.instant("CLOSE"), {
            duration: 3000,
          })
        },
        error: (error) => {
          this.message = error.message || this.translate.instant("ERRORS.USERNAME_UPDATE_ERROR")
          this.isLoading = false
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        },
      })
    } else if (this.editField === "email") {
      this.userService.updateEmail(this.userId, this.newEmail).subscribe({
        next: (response) => {
          this.authService.currentUser!.email = this.newEmail
          this.cancelEdit()
          this.isLoading = false
          this.snackBar.open(this.translate.instant("SUCCESS.EMAIL_UPDATED"), this.translate.instant("CLOSE"), {
            duration: 3000,
          })
        },
        error: (error) => {
          this.message = error.message || this.translate.instant("ERRORS.EMAIL_UPDATE_ERROR")
          this.isLoading = false
          this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
            duration: 5000,
            panelClass: ["error-snackbar"],
          })
        },
      })
    } else if (this.editField === "password") {
      if (this.isGoogleAuthUser) {
        if (!this.passwordForm.get("newPassword")?.valid) {
          this.isLoading = false
          this.passwordForm.markAllAsTouched()
          return
        }

        this.userService.setPassword(this.userId, this.newPassword).subscribe({
          next: (response) => {
            this.isGoogleAuthUser = false
            this.cancelEdit()
            this.isLoading = false
            this.snackBar.open(this.translate.instant("SUCCESS.PASSWORD_SET"), this.translate.instant("CLOSE"), {
              duration: 3000,
            })
          },
          error: (error) => {
            this.message = error.message || this.translate.instant("ERRORS.PASSWORD_SET_ERROR")
            this.isLoading = false
            this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
              duration: 5000,
              panelClass: ["error-snackbar"],
            })
          },
        })
      } else {
        if (!this.passwordForm.valid) {
          this.isLoading = false
          this.passwordForm.markAllAsTouched()
          return
        }

        this.currentPassword = this.passwordForm.get("currentPassword")?.value || ""

        this.userService.updatePassword(this.userId, this.currentPassword, this.newPassword).subscribe({
          next: (response) => {
            this.cancelEdit()
            this.isLoading = false
            this.snackBar.open(this.translate.instant("SUCCESS.PASSWORD_UPDATED"), this.translate.instant("CLOSE"), {
              duration: 3000,
            })
          },
          error: (error) => {
            this.message = error.message || this.translate.instant("ERRORS.PASSWORD_UPDATE_ERROR")
            this.isLoading = false
            this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
              duration: 5000,
              panelClass: ["error-snackbar"],
            })
          },
        })
      }
    }
  }

  deleteProfile(): void {
    if (!this.password) {
      this.message = this.translate.instant("ERRORS.PASSWORD_REQUIRED_DELETE")
      return
    }

    this.isLoading = true

    this.userService.deleteAccount(this.userId, this.password).subscribe({
      next: (response) => {
        this.isLoading = false
        this.snackBar.open(this.translate.instant("SUCCESS.PROFILE_DELETED"), this.translate.instant("CLOSE"), {
          duration: 3000,
        })
        this.router.navigate(["/login"]).then(() => {
          window.location.reload()
        })
      },
      error: (error) => {
        this.message = error.message || this.translate.instant("ERRORS.PROFILE_DELETE_ERROR")
        this.isLoading = false
        this.snackBar.open(this.message, this.translate.instant("CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
      },
    })
  }

  cancelEdit(): void {
    this.editField = ""
    this.newUsername = ""
    this.newEmail = ""
    this.currentPassword = ""
    this.newPassword = ""
    this.message = ""
    this.resetPasswordStrengthInfo()
    this.passwordForm.reset()
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "350px",
      data: {
        title: this.translate.instant("PROFILE.DELETE_ACCOUNT"),
        message: this.translate.instant("PROFILE.DELETE_WARNING"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.toggleDeleteForm()
      }
    })
  }

  getPasswordErrorMessage(controlName: string): string {
    const control = this.passwordForm.get(controlName)
    if (!control) return ""

    if (control.hasError("required")) {
      return this.translate.instant("ERRORS.PASSWORD_REQUIRED")
    }

    if (control.hasError("minlength")) {
      return this.translate.instant("ERRORS.PASSWORD_MIN_LENGTH")
    }

    if (control.hasError("passwordStrength")) {
      return this.passwordValidator.getPasswordErrorMessage(control.errors)
    }

    return ""
  }

  changePalette(palette: string): void {
    const tempSelectedPalette = palette

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "350px",
      data: {
        title: this.translate.instant("PROFILE.THEME_UPDATED"),
        message: this.translate.instant("PROFILE.RELOAD_REQUIRED"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.selectedPalette = tempSelectedPalette
        this.themeService.setPalette(tempSelectedPalette)
        this.themeService.applyPaletteChanges()
      }
    })
  }

  getPaletteDisplayName(value: string): string {
    const palette = this.availablePalettes.find((p) => p.value === value)
    return palette ? palette.displayName : value
  }
}
