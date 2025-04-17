import { Component, Input, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router, RouterLink} from "@angular/router";
import { AuthService } from "../../services/auth/auth.service";
import { Ticket } from "../../models/ticket.model";
import { NgIf, DatePipe, NgClass } from "@angular/common";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatIcon } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TicketService } from '../../services/ticket/ticket.service';

@Component({
  selector: "app-ticket-list",
  templateUrl: "./ticket-list.component.html",
  styleUrls: ["./ticket-list.component.scss"],
  standalone: true,
  imports: [
    NgIf,
    DatePipe,
    NgClass,
    MatProgressSpinner,
    MatButton,
    MatIconButton,
    MatError,
    MatLabel,
    MatInput,
    MatFormField,
    ReactiveFormsModule,
    MatIcon,
    MatTableModule,
    TranslateModule,
    RouterLink
  ],
})
export class TicketListComponent implements OnInit {
  @Input() showMyTickets: boolean = true;

  isLoggedIn = false;
  userTickets: Ticket[] = [];
  isLoading = false;
  referenceForm: FormGroup;
  searchedTicket: Ticket | null = null;
  isSearching = false;
  displayedColumns: string[] = ['reference', 'subject', 'status', 'createdAt', 'actions'];

  constructor(
    private ticketService: TicketService,
    private authService: AuthService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private router: Router,
    private translate: TranslateService
  ) {
    this.referenceForm = this.fb.group({
      reference: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]]
    });
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();

    if (this.isLoggedIn && this.showMyTickets) {
      this.loadUserTickets();
    }
  }

  loadUserTickets(): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.isLoading = true;
      this.ticketService.getUserTickets(userId).subscribe(
        (tickets) => {
          this.userTickets = tickets;
          console.log(this.userTickets);
          this.isLoading = false;
        },
        (error) => {
          this.showError(this.translate.instant('TICKET.ERROR_LOADING'));
          this.isLoading = false;
        }
      );
    }
  }

  searchByReference(): void {
    if (this.referenceForm.valid) {
      this.isSearching = true;
      this.searchedTicket = null;

      const reference = this.referenceForm.value.reference.toUpperCase();

      this.ticketService.getTicketByReference(reference).subscribe(
        (ticket) => {
          this.searchedTicket = ticket;
          this.isSearching = false;
        },
        (error) => {
          this.showError(this.translate.instant('TICKET.REFERENCE_NOT_FOUND'));
          this.isSearching = false;
        }
      );
    }
  }

  viewTicket(ticketId: number): void {
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

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" });
  }
}
