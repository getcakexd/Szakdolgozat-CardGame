import { Component, OnInit } from "@angular/core"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { User } from "../../models/user.model"
import { ModifyUserDialogComponent } from "../modify-user-dialog/modify-user-dialog.component"
import { UserHistoryDialogComponent } from "../user-history-dialog/user-history-dialog.component"
import {ContactRequest} from '../../models/contact-request.model';
import {UserHistory} from '../../models/user-history.model';
import {UserService} from '../../services/user/user.service';
import {ContactService} from '../../services/contact/contact.service';
import {ConfirmDialogComponent} from '../confirm-dialog/confirm-dialog.component';
import {
  MatCell,
  MatCellDef, MatColumnDef, MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef, MatTable
} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {DatePipe, NgClass, NgIf} from '@angular/common';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';

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
    MatCard
  ],
  standalone: true
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
  ) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadContactRequests()
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => (this.users = users),
      (error) => this.showError("Failed to load users"),
    )
  }

  loadContactRequests(): void {
    this.contactService.getAllContactRequests().subscribe(
      (requests) => (this.contactRequests = requests),
      (error) => this.showError("Failed to load contact requests"),
    )
  }

  unlockUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: { title: "Confirm Unlock", message: `Are you sure you want to unlock user ${user.username}?` },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.unlockUser(user.id).subscribe(
          () => {
            this.showSuccess("User unlocked successfully")
            this.loadUsers()
          },
          (error) => this.showError("Failed to unlock user"),
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
            this.showSuccess("User modified successfully")
            this.loadUsers()
          },
          (error) => this.showError("Failed to modify user"),
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
      (error) => this.showError("Failed to load user history"),
    )
  }

  updateContactStatus(request: ContactRequest, status: "in-progress" | "resolved"): void {
    const currentUser = JSON.parse(localStorage.getItem("currentUser") || "{}")
    this.contactService.updateContactRequestStatus(request.id!, status, currentUser.id).subscribe(
      () => {
        this.showSuccess(`Request marked as ${status}`)
        this.loadContactRequests()
      },
      (error) => this.showError("Failed to update request status"),
    )
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, "Close", { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, "Close", { duration: 5000, panelClass: "error-snackbar" })
  }
}

