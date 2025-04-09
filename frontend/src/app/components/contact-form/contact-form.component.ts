import { Component, OnInit, ViewEncapsulation } from "@angular/core"
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms"
import { MatSnackBar } from "@angular/material/snack-bar"
import { ContactService } from "../../services/contact/contact.service"
import { ContactRequest } from "../../models/contact-request.model"
import { NgIf } from "@angular/common"
import { MatProgressSpinner } from "@angular/material/progress-spinner"
import { MatButton, MatIconButton } from "@angular/material/button"
import { MatError, MatFormField, MatLabel, MatHint } from "@angular/material/form-field"
import { MatInput } from "@angular/material/input"
import { MatIcon } from "@angular/material/icon"
import { MatCard } from "@angular/material/card"
import { MatOption } from "@angular/material/core"
import { MatSelect } from "@angular/material/select"
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-contact-form",
  templateUrl: "./contact-form.component.html",
  styleUrls: ["./contact-form.component.scss"],
  encapsulation: ViewEncapsulation.None,
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
    MatIcon,
    MatIconButton,
    MatCard,
    MatOption,
    MatSelect,
    MatHint,
    TranslateModule
  ],
})
export class ContactFormComponent implements OnInit {
  contactForm: FormGroup
  isSubmitting = false
  isExpanded = false
  customPanelClass = "problem-type-panel"

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.contactForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(2)]],
      email: ["", [Validators.required, Validators.email]],
      problemType: ["", Validators.required],
      subject: ["", Validators.required],
      message: ["", [Validators.required, Validators.minLength(10)]],
    })
  }

  ngOnInit(): void {
  }

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded
  }

  onSubmit(): void {
    if (this.contactForm.valid) {
      this.isSubmitting = true

      const request: ContactRequest = {
        ...this.contactForm.value,
        status: "new",
        createdAt: new Date(),
      }

      this.contactService.submitContactRequest(request).subscribe(
        () => {
          this.isSubmitting = false
          this.contactForm.reset()
          this.isExpanded = false
          this.showSuccess(this.translate.instant('CONTACT.SUCCESS_MESSAGE'))
        },
        (error) => {
          this.isSubmitting = false
          this.showError(this.translate.instant('CONTACT.ERROR_MESSAGE'))
        },
      )
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" })
  }
}
