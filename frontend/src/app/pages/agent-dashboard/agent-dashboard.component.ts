import { Component, OnInit } from "@angular/core"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { Router } from "@angular/router"
import { User } from "../../models/user.model"
import { ModifyUserDialogComponent } from "../../components/modify-user-dialog/modify-user-dialog.component"
import { UserHistoryDialogComponent } from "../../components/user-history-dialog/user-history-dialog.component"
import { Ticket } from "../../models/ticket.model"
import { UserHistory } from "../../models/user-history.model"
import { UserService } from "../../services/user/user.service"
import { ConfirmDialogComponent } from "../../components/confirm-dialog/confirm-dialog.component"
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
import {MatButton, MatIconButton} from "@angular/material/button"
import { DatePipe, NgClass, NgIf } from "@angular/common"
import { MatTab, MatTabGroup } from "@angular/material/tabs"
import { MatCard } from "@angular/material/card"
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {TicketService} from '../../services/ticket/ticket.service';

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
    MatCard,
    TranslateModule,
    MatButton
  ],
  standalone: true,
})
export class AgentDashboardComponent implements OnInit {
  users: User[] = []
  tickets: Ticket[] = []
  userDisplayedColumns: string[] = ["id", "username", "email", "locked", "actions"]
  ticketDisplayedColumns: string[] = ["reference", "name", "email", "subject", "status", "createdAt", "actions"]
  userHistory: UserHistory[] = []
  activeFilter: 'all' | 'new' | 'in-progress' | 'resolved' = 'all'
  selectedTabIndex = 0;

  constructor(
    private userService: UserService,
    private ticketService: TicketService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadTickets()
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => (this.users = users),
      () => this.showError(this.translate.instant('AGENT.FAILED_LOAD_USERS')),
    )
  }

  loadTickets(): void {
    this.ticketService.getAllTickets().subscribe(
      (tickets) => (this.tickets = tickets),
      () => this.showError(this.translate.instant('TICKET.ERROR_LOADING')),
    )
  }

  applyFilter(filter: 'all' | 'new' | 'in-progress' | 'resolved'): void {
    this.activeFilter = filter;
  }

  getFilteredTickets(): Ticket[] {
    if (this.activeFilter === 'all') {
      return this.tickets;
    } else {
      return this.tickets.filter(ticket => ticket.status === this.activeFilter);
    }
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
          () => this.showError(this.translate.instant('AGENT.FAILED_MODIFY_USER')),
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
      () => this.showError(this.translate.instant('AGENT.FAILED_LOAD_HISTORY')),
    )
  }

  updateTicketStatus(ticket: Ticket, status: 'in-progress' | 'resolved'): void {
    const currentUser = JSON.parse(localStorage.getItem("currentUser") || "{}")
    this.ticketService.updateTicketStatus(ticket.id!.toString(), status, currentUser.id).subscribe(
      (updatedTicket) => {
        const index = this.tickets.findIndex(t => t.id === updatedTicket.id);
        if (index !== -1) {
          this.tickets[index] = updatedTicket;
        }
        this.showSuccess(this.translate.instant('TICKET.STATUS_UPDATED'));
        this.loadTickets();
        this.selectedTabIndex = 1;
      },
      () => this.showError(this.translate.instant('TICKET.ERROR_UPDATING_STATUS')),
    )
  }

  viewTicket(ticketId: string): void {
    this.router.navigate(['/ticket', ticketId]);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'new': return 'status-new';
      case 'in-progress': return 'status-progress';
      case 'resolved': return 'status-resolved';
      default: return '';
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" })
  }
}
