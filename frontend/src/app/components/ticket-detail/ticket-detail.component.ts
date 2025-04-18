import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from "../../services/auth/auth.service";
import { Ticket, TicketMessage } from "../../models/ticket.model";
import { NgIf, NgFor, DatePipe, NgClass } from "@angular/common";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatIcon } from "@angular/material/icon";
import {
  MatCard,
  MatCardTitle,
  MatCardContent,
  MatCardActions,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitleGroup
} from "@angular/material/card";
import { MatDivider } from "@angular/material/divider";
import { MatList, MatListItem, MatListItemTitle, MatListItemLine } from "@angular/material/list";
import { MatChip } from "@angular/material/chips";
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TicketService } from '../../services/ticket/ticket.service';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: "app-ticket-detail",
  templateUrl: "./ticket-detail.component.html",
  styleUrls: ["./ticket-detail.component.scss"],
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    DatePipe,
    NgClass,
    MatProgressSpinner,
    MatButton,
    MatError,
    MatLabel,
    MatInput,
    MatFormField,
    ReactiveFormsModule,
    MatIcon,
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatCardActions,
    MatCardHeader,
    MatCardSubtitle,
    MatDivider,
    MatList,
    MatListItem,
    MatListItemTitle,
    MatListItemLine,
    MatChip,
    TranslateModule,
    MatCardTitleGroup
  ],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('300ms ease-in', style({ opacity: 1 })),
      ]),
    ]),
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateY(20px)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
      ]),
    ]),
  ],
})
export class TicketDetailComponent implements OnInit {
  ticket: Ticket | null = null;
  messages: TicketMessage[] = [];
  isLoading = true;
  messageForm: FormGroup;
  isSending = false;
  isLoggedIn = false;
  isAgent = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ticketService: TicketService,
    private authService: AuthService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.messageForm = this.fb.group({
      message: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.isAgent = this.authService.isAgent() || this.authService.isAdmin() || this.authService.isRoot();

    const ticketId = this.route.snapshot.paramMap.get('id');
    if (ticketId) {
      this.loadTicket(ticketId);
    } else {
      this.router.navigate(['/tickets']);
    }
  }

  loadTicket(ticketId: string): void {
    this.isLoading = true;

    this.ticketService.getTicketById(ticketId).subscribe(
      (ticket) => {
        this.ticket = ticket;
        this.loadMessages(ticketId);
      },
      (error) => {
        this.showError(this.translate.instant('TICKET.ERROR_LOADING'));
        this.isLoading = false;
        this.router.navigate(['/tickets']);
      }
    );
  }

  loadMessages(ticketId: string): void {
    this.ticketService.getTicketMessages(ticketId).subscribe(
      (messages) => {
        this.messages = messages;
        this.isLoading = false;
      },
      (error) => {
        this.showError(this.translate.instant('TICKET.ERROR_LOADING_MESSAGES'));
        this.isLoading = false;
      }
    );
  }

  sendMessage(): void {
    if (this.messageForm.valid && this.ticket) {
      if (this.ticket.status === 'new' && !this.isAgent) {
        this.showError(this.translate.instant('TICKET.CANNOT_REPLY_TO_NEW'));
        return;
      }

      this.isSending = true;

      const currentUser = this.authService.currentUser;
      console.log(this.isAgent)
      const senderType = this.isAgent ? 'agent' : 'user';
      const senderName = currentUser ? currentUser.username : this.ticket.name;

      const message: TicketMessage = {
        ticketId: this.ticket.id!,
        userId: currentUser?.id,
        senderName: senderName,
        senderType: senderType,
        message: this.messageForm.value.message,
        createdAt: new Date()
      };

      this.ticketService.addTicketMessage(message).subscribe(
        (response) => {
          this.messages.push(response);
          this.messageForm.reset();
          this.isSending = false;
        },
        (error) => {
          this.showError(this.translate.instant('TICKET.ERROR_SENDING_MESSAGE'));
          this.isSending = false;
        }
      );
    }
  }

  updateStatus(status: 'in-progress' | 'resolved'): void {
    if (this.ticket && this.isAgent) {
      const agentId = this.authService.getCurrentUserId();

      if (agentId) {
        this.ticketService.updateTicketStatus(this.ticket.id!.toString(), status, agentId).subscribe(
          (updatedTicket) => {
            this.ticket = updatedTicket;
            this.showSuccess(this.translate.instant('TICKET.STATUS_UPDATED'));
          },
          (error) => {
            this.showError(this.translate.instant('TICKET.ERROR_UPDATING_STATUS'));
          }
        );
      }
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'new': return 'status-new';
      case 'in-progress': return 'status-progress';
      case 'resolved': return 'status-resolved';
      default: return '';
    }
  }

  canReplyToTicket(): boolean {
    if (!this.ticket) return false;
    if (this.ticket.status === 'resolved') return false;
    if (this.ticket.status === 'new' && !this.isAgent) return false;
    return true;
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 3000 });
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" });
  }
}
