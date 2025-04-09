import { Component, OnInit } from "@angular/core"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { User } from "../../models/user.model"
import { ModifyUserDialogComponent } from "../modify-user-dialog/modify-user-dialog.component"
import { UserHistoryDialogComponent } from "../user-history-dialog/user-history-dialog.component"
import { ContactRequest } from "../../models/contact-request.model"
import { UserHistory } from "../../models/user-history.model"
import { UserService } from "../../services/user/user.service"
import { ContactService } from "../../services/contact/contact.service"
import { ConfirmDialogComponent } from "../confirm-dialog/confirm-dialog.component"
import { ContactDetailsDialogComponent } from "../contact-details-dialog/contact-details-dialog.component"
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
} from "@angular/material/table"
import { MatIcon } from "@angular/material/icon"
import { MatIconButton } from "@angular/material/button"
import { DatePipe, NgClass, NgIf } from "@angular/common"
import { MatTab, MatTabGroup } from "@angular/material/tabs"
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from "@angular/material/card"
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-agent-dashboard",
  templateUrl: "./agent-dashboard.component.html",
  styleUrls: ["./agent-dashboard.component.scss"],
  imports: [
    MatHeaderRow,
    MatHeaderRowDef,
    MatRowDef,
    MatIcon,
    MatIconButton,
    MatRow,
    MatCellDef,
    MatHeaderCellDef,
    NgIf,
    MatCell,
    MatHeaderCell,
    MatColumnDef,
    DatePipe,
    NgClass,
    MatTable,
    MatTab,
    MatTabGroup,
    MatCardContent,
    MatCardTitle,
    MatCardHeader,
    MatCard,
    TranslateModule
  ],
  standalone: true,
})
export class AgentDashboardComponent implements OnInit {
  users: User[] = []
  contactRequests: ContactRequest[] = []
  userDisplayedColumns: string[] = ["id", "username", "email", "locked", "actions"]
  contactDisplayedColumns: string[] = ["id", "name", "email", "subject", "status", "createdAt", "actions"]
  selectedUser: User | null = null
  userHistory: UserHistory[] = []

  constructor(
    private userService: UserService,
    private contactService: ContactService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadContactRequests()
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => (this.users = users),
      (error) => this.showError(this.translate.instant('AGENT.FAILED_LOAD_USERS')),
    )
  }

  loadContactRequests(): void {
    this.contactService.getAllContactRequests().subscribe(
      (requests) => (this.contactRequests = requests),
      (error) => this.showError(this.translate.instant('AGENT.FAILED_LOAD_REQUESTS')),
    )
  }

  unlockUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('AGENT.CONFIRM_UNLOCK', { username: user.username })
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.unlockUser(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('AGENT.USER_UNLOCKED'))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('AGENT.FAILED_UNLOCK_USER')),
        )
      }
    })
  }

  modifyUser(user: User): void {
    const dialogRef = this.dialog.open(ModifyUserDialogComponent, {
      width: "400px",
      data: { user },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.modifyUserData(user.id, result).subscribe(
          () => {
            this.showSuccess(this.translate.instant('AGENT.USER_MODIFIED'))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('AGENT.FAILED_MODIFY_USER')),
        )
      }
    })
  }

  viewUserHistory(user: User): void {
    this.userService.getUserHistory(user.id).subscribe(
      (history) => {
        this.userHistory = history
        this.dialog.open(UserHistoryDialogComponent, {
          width: "600px",
          data: { user, history },
        })
      },
      (error) => this.showError(this.translate.instant('AGENT.FAILED_LOAD_HISTORY')),
    )
  }

  updateContactStatus(request: ContactRequest, status: "in-progress" | "resolved"): void {
    const currentUser = JSON.parse(localStorage.getItem("currentUser") || "{}")
    this.contactService.updateContactRequestStatus(request.id!, status, currentUser.id).subscribe(
      () => {
        this.showSuccess(this.translate.instant('AGENT.REQUEST_MARKED', { status: status }))
        this.loadContactRequests()
      },
      (error) => this.showError(this.translate.instant('AGENT.FAILED_UPDATE_STATUS')),
    )
  }

  viewContactDetails(request: ContactRequest): void {
    this.dialog.open(ContactDetailsDialogComponent, {
      width: "600px",
      data: { request },
    })
  }

  sendEmail(request: ContactRequest): void {
    this.snackBar.open(
      this.translate.instant('AGENT.EMAIL_FUTURE'),
      this.translate.instant('COMMON.CLOSE'),
      { duration: 3000 }
    )
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" })
  }
}
