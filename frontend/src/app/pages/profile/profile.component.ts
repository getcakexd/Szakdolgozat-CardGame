import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../services/auth/auth.service';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-profile",
  imports: [
    FormsModule,
    NgIf,
    MatCardModule,
    MatFormFieldModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatDialogModule,
    MatProgressBarModule,
    TranslateModule
  ],
  templateUrl: "./profile.component.html",
  standalone: true,
  styleUrls: ["./profile.component.css"],
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

  private deleteSource = new Subject<void>()
  delete$ = this.deleteSource.asObservable()
  private userId: number

  constructor(
    protected userService: UserService,
    protected authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private translate: TranslateService
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(["/login"]).then()
    }
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
          console.error("Error checking Google auth status:", error)
          this.isLoading = false
        },
      })
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
          this.snackBar.open(
            this.translate.instant('SUCCESS.USERNAME_UPDATED'),
            this.translate.instant('CLOSE'),
            { duration: 3000 }
          )
        },
        error: (error) => {
          this.message = error.message || this.translate.instant('ERRORS.USERNAME_UPDATE_ERROR')
          this.isLoading = false
          this.snackBar.open(
            this.message,
            this.translate.instant('CLOSE'),
            { duration: 5000, panelClass: ["error-snackbar"] }
          )
        },
      })
    } else if (this.editField === "email") {
      this.userService.updateEmail(this.userId, this.newEmail).subscribe({
        next: (response) => {
          this.authService.currentUser!.email = this.newEmail
          this.cancelEdit()
          this.isLoading = false
          this.snackBar.open(
            this.translate.instant('SUCCESS.EMAIL_UPDATED'),
            this.translate.instant('CLOSE'),
            { duration: 3000 }
          )
        },
        error: (error) => {
          this.message = error.message || this.translate.instant('ERRORS.EMAIL_UPDATE_ERROR')
          this.isLoading = false
          this.snackBar.open(
            this.message,
            this.translate.instant('CLOSE'),
            { duration: 5000, panelClass: ["error-snackbar"] }
          )
        },
      })
    } else if (this.editField === "password") {
      if (this.isGoogleAuthUser) {
        this.userService.setPassword(this.userId, this.newPassword).subscribe({
          next: (response) => {
            this.isGoogleAuthUser = false
            this.cancelEdit()
            this.isLoading = false
            this.snackBar.open(
              this.translate.instant('SUCCESS.PASSWORD_SET'),
              this.translate.instant('CLOSE'),
              { duration: 3000 }
            )
          },
          error: (error) => {
            this.message = error.message || this.translate.instant('ERRORS.PASSWORD_SET_ERROR')
            this.isLoading = false
            this.snackBar.open(
              this.message,
              this.translate.instant('CLOSE'),
              { duration: 5000, panelClass: ["error-snackbar"] }
            )
          },
        })
      } else {
        this.userService.updatePassword(this.userId, this.currentPassword, this.newPassword).subscribe({
          next: (response) => {
            this.cancelEdit()
            this.isLoading = false
            this.snackBar.open(
              this.translate.instant('SUCCESS.PASSWORD_UPDATED'),
              this.translate.instant('CLOSE'),
              { duration: 3000 }
            )
          },
          error: (error) => {
            this.message = error.message || this.translate.instant('ERRORS.PASSWORD_UPDATE_ERROR')
            this.isLoading = false
            this.snackBar.open(
              this.message,
              this.translate.instant('CLOSE'),
              { duration: 5000, panelClass: ["error-snackbar"] }
            )
          },
        })
      }
    }
  }

  deleteProfile(): void {
    if (!this.password) {
      this.message = this.translate.instant('ERRORS.PASSWORD_REQUIRED_DELETE')
      return
    }

    this.isLoading = true

    this.userService.deleteAccount(this.userId, this.password).subscribe({
      next: (response) => {
        this.isLoading = false
        this.snackBar.open(
          this.translate.instant('SUCCESS.PROFILE_DELETED'),
          this.translate.instant('CLOSE'),
          { duration: 3000 }
        )
        this.router.navigate(["/login"]).then(() => {
          window.location.reload()
        })
      },
      error: (error) => {
        this.message = error.message || this.translate.instant('ERRORS.PROFILE_DELETE_ERROR')
        this.isLoading = false
        this.snackBar.open(
          this.message,
          this.translate.instant('CLOSE'),
          { duration: 5000, panelClass: ["error-snackbar"] }
        )
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
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "350px",
      data: {
        title: this.translate.instant('PROFILE.DELETE_ACCOUNT'),
        message: this.translate.instant('PROFILE.DELETE_WARNING'),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.toggleDeleteForm()
      }
    })
  }
}
