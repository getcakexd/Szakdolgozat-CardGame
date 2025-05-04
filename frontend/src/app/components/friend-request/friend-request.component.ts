import { Component, OnInit } from "@angular/core"
import { FriendRequestService } from "../../services/friend-request/friend-request.service"
import { FormsModule } from "@angular/forms"
import { NgForOf, NgIf } from "@angular/common"
import { FriendRequest } from "../../models/FriendRequest"
import { MatCardModule } from "@angular/material/card"
import { MatListModule } from "@angular/material/list"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatDividerModule } from "@angular/material/divider"
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { MatTooltipModule } from "@angular/material/tooltip"
import { MatChipsModule } from "@angular/material/chips"
import { AuthService } from "../../services/auth/auth.service"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"

@Component({
  selector: "app-friend-request",
  templateUrl: "./friend-request.component.html",
  styleUrls: ["./friend-request.component.scss"],
  imports: [
    FormsModule,
    NgIf,
    NgForOf,
    MatCardModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule,
    MatSnackBarModule,
    TranslateModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  standalone: true,
})
export class FriendRequestComponent implements OnInit {
  username = ""
  errorMessage = ""
  sentRequests: FriendRequest[] = []
  incomingRequests: FriendRequest[] = []
  currentUserId: number
  isLoading = false
  isSubmitting = false

  constructor(
    private authService: AuthService,
    private friendRequestService: FriendRequestService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {
    this.currentUserId = this.authService.getCurrentUserId() || 0
  }

  ngOnInit(): void {
    this.loadRequests()
  }

  loadRequests() {
    this.isLoading = true

    this.friendRequestService.getSentRequests(this.currentUserId).subscribe({
      next: (requests) => {
        this.sentRequests = requests
        this.checkIncomingRequests()
      },
      error: (error) => {
        this.isLoading = false
        this.showErrorSnackbar("SOCIAL.FAILED_LOAD_SENT_REQUESTS")
      },
    })
  }

  checkIncomingRequests() {
    this.friendRequestService.getIncomingRequests(this.currentUserId).subscribe({
      next: (requests) => {
        this.incomingRequests = requests
        this.isLoading = false
      },
      error: (error) => {
        this.isLoading = false
        this.showErrorSnackbar("SOCIAL.FAILED_LOAD_INCOMING_REQUESTS")
      },
    })
  }

  sendRequest() {
    if (!this.username.trim()) {
      this.errorMessage = this.translate.instant("SOCIAL.USERNAME_EMPTY")
      return
    }

    this.isSubmitting = true
    this.errorMessage = ""

    this.friendRequestService.sendFriendRequest(this.currentUserId, this.username).subscribe({
      next: () => {
        this.loadRequests()
        this.username = ""
        this.isSubmitting = false
        this.showSuccessSnackbar("SOCIAL.REQUEST_SENT")
      },
      error: (error) => {
        this.errorMessage = error.error?.message || this.translate.instant("SOCIAL.FAILED_SEND_REQUEST")
        this.isSubmitting = false
        this.showErrorSnackbar(this.errorMessage, 5000)
      },
    })
  }

  clearUsername() {
    this.username = ""
    this.errorMessage = ""
  }

  cancelRequest(requestId: number) {
    this.friendRequestService.cancelFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests()
        this.showSuccessSnackbar("SOCIAL.REQUEST_CANCELLED")
      },
      error: (error) => {
        this.showErrorSnackbar("SOCIAL.FAILED_CANCEL_REQUEST")
      },
    })
  }

  acceptRequest(requestId: number) {
    this.friendRequestService.acceptFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests()
        this.showSuccessSnackbar("SOCIAL.REQUEST_ACCEPTED")
        window.location.reload()
      },
      error: (error) => {
        this.showErrorSnackbar("SOCIAL.FAILED_ACCEPT_REQUEST")
      },
    })
  }

  declineRequest(requestId: number) {
    this.friendRequestService.declineFriendRequest(requestId).subscribe({
      next: () => {
        this.loadRequests()
        this.showSuccessSnackbar("SOCIAL.REQUEST_DECLINED")
      },
      error: (error) => {
        this.showErrorSnackbar("SOCIAL.FAILED_DECLINE_REQUEST")
      },
    })
  }

  getStatusColor(status: string): string {
    switch (status) {
      case "accepted":
        return "primary"
      case "pending":
        return "accent"
      case "declined":
        return "warn"
      default:
        return ""
    }
  }

  private showSuccessSnackbar(messageKey: string, duration = 3000) {
    this.snackBar.open(this.translate.instant(messageKey), this.translate.instant("COMMON.CLOSE"), {
      duration: duration,
    })
  }

  private showErrorSnackbar(messageKey: string, duration = 3000) {
    this.snackBar.open(this.translate.instant(messageKey), this.translate.instant("COMMON.CLOSE"), {
      duration: duration,
      panelClass: ["error-snackbar"],
    })
  }
}
