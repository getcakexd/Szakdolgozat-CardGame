import { Component, Inject } from "@angular/core"
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions, MatDialogClose,
} from "@angular/material/dialog"
import { MatButton } from "@angular/material/button"
import { DatePipe, NgClass } from "@angular/common"
import { ContactRequest } from "../../models/contact-request.model"
import { MatSnackBar } from "@angular/material/snack-bar"
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-contact-details-dialog",
  templateUrl: "./contact-details-dialog.component.html",
  styleUrls: ["./contact-details-dialog.component.scss"],
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    DatePipe,
    NgClass,
    MatDialogClose,
    TranslateModule
  ],
})
export class ContactDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ContactDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { request: ContactRequest },
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  formatProblemType(type: string): string {
    if (!type) return this.translate.instant('AGENT.NOT_SPECIFIED');

    const types: { [key: string]: string } = {
      "locked-account": this.translate.instant('AGENT.LOCKED_ACCOUNT'),
      "stolen-account": this.translate.instant('AGENT.STOLEN_ACCOUNT'),
      "report-user": this.translate.instant('AGENT.REPORT_USER'),
      "other": this.translate.instant('AGENT.OTHER')
    }

    return types[type] || type
  }

  sendEmail(): void {
    this.snackBar.open(
      this.translate.instant('AGENT.EMAIL_FUTURE'),
      this.translate.instant('COMMON.CLOSE'),
      { duration: 3000 }
    )
  }
}
