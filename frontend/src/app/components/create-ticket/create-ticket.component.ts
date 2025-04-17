import { Component, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { AuthService } from "../../services/auth/auth.service";
import { Ticket, TicketMessage } from "../../models/ticket.model";
import { NgIf } from "@angular/common";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import { MatButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel, MatHint } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatCard, MatCardContent } from "@angular/material/card";
import { MatOption } from "@angular/material/core";
import { MatSelect } from "@angular/material/select";
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { TicketService } from '../../services/ticket/ticket.service';

@Component({
  selector: "app-create-ticket",
  templateUrl: "./create-ticket.component.html",
  styleUrls: ["./create-ticket.component.scss"],
  standalone: true,
  imports: [
    NgIf,
    MatProgressSpinner,
    MatButton,
    MatError,
    MatLabel,
    MatInput,
    MatFormField,
    ReactiveFormsModule,
    MatCard,
    MatCardContent,
    MatOption,
    MatSelect,
    MatHint,
    TranslateModule,
  ],
})
export class CreateTicketComponent implements OnInit {
  ticketForm: FormGroup;
  isSubmitting = false;
  isLoggedIn = false;
  ticketReference: string | null = null;

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    private translate: TranslateService
  ) {
    this.ticketForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(2)]],
      email: ["", [Validators.required, Validators.email]],
      category: ["", Validators.required],
      subject: ["", Validators.required],
      message: ["", [Validators.required, Validators.minLength(10)]],
    });
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();

    if (this.isLoggedIn) {
      const currentUser = this.authService.currentUser;
      if (currentUser) {
        this.ticketForm.patchValue({
          name: currentUser.username,
          email: currentUser.email
        });
      }
    }
  }

  onSubmit(): void {
    if (this.ticketForm.valid) {
      this.isSubmitting = true;

      const reference = this.ticketService.generateTicketReference();
      const userId = this.authService.getCurrentUserId() || null;
      console.log(userId)

      const ticket: Ticket = {
        userId: userId || undefined,
        name: this.ticketForm.value.name,
        email: this.ticketForm.value.email,
        subject: this.ticketForm.value.subject,
        status: 'new',
        createdAt: new Date(),
        category: this.ticketForm.value.category,
        reference: reference
      };

      console.log(ticket);

      this.ticketService.createTicket(ticket).subscribe(
        (response) => {
          this.isSubmitting = false;

          const initialMessage: TicketMessage = {
            ticketId: response.id!,
            senderName: ticket.name,
            senderType: 'user',
            message: this.ticketForm.value.message,
            createdAt: new Date()
          };

          this.ticketService.addTicketMessage(initialMessage).subscribe(
            () => {
              if (this.isLoggedIn) {
                window.location.reload();
                this.showSuccess(this.translate.instant('TICKET.SUCCESS_MESSAGE'));
              } else {
                this.ticketReference = reference;
                this.ticketForm.reset();
                this.showSuccess(this.translate.instant('TICKET.SUCCESS_REFERENCE', { reference }));
              }
            },
            (error) => {
              console.error('Error adding message:', error);
              this.showError(this.translate.instant('TICKET.ERROR_MESSAGE'));
            }
          );
        },
        (error) => {
          console.error('Error creating ticket:', error);
          this.isSubmitting = false;
          this.showError(this.translate.instant('TICKET.ERROR_MESSAGE'));
        }
      );
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000 });
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" });
  }

  protected readonly navigator = navigator;
}
