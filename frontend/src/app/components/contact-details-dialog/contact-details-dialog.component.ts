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

@Component({
  selector: "app-contact-details-dialog",
  templateUrl: "./contact-details-dialog.component.html",
  styleUrls: ["./contact-details-dialog.component.scss"],
  standalone: true,
  imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, DatePipe, NgClass, MatDialogClose],
})
export class ContactDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ContactDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { request: ContactRequest },
    private snackBar: MatSnackBar
  ) {}

  formatProblemType(type: string): string {
    if (!type) return "Not specified"

    const types: { [key: string]: string } = {
      "locked-account": "Locked Account",
      "stolen-account": "Stolen Account",
      "report-user": "Reporting Another User",
      other: "Other",
    }

    return types[type] || type
  }

  sendEmail(): void {
    this.snackBar.open("Email functionality will be implemented in the future.", "Close", {
      duration: 3000,
    })
  }
}

