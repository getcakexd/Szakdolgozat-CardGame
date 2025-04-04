import { Component, Inject } from "@angular/core"
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogTitle
} from "@angular/material/dialog"
import { User } from "../../models/user.model"
import {UserHistory} from '../../models/user-history.model';
import {
  MatCell,
  MatCellDef, MatColumnDef, MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable
} from '@angular/material/table';
import {DatePipe, NgIf} from '@angular/common';
import {MatButton} from '@angular/material/button';

@Component({
  selector: "app-user-history-dialog",
  templateUrl: "./user-history-dialog.component.html",
  styleUrls: ["./user-history-dialog.component.scss"],
  imports: [
    MatHeaderRow,
    MatHeaderRowDef,
    MatRowDef,
    MatRow,
    NgIf,
    MatDialogContent,
    MatTable,
    DatePipe,
    MatCellDef,
    MatHeaderCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatDialogActions,
    MatButton,
    MatDialogClose,
    MatDialogTitle
  ],
  standalone: true
})
export class UserHistoryDialogComponent {
  displayedColumns: string[] = ["changedAt", "changedBy", "previousUsername", "previousEmail"];

  constructor(@Inject(MAT_DIALOG_DATA) public data: { user: User, history: UserHistory[] }) {}
}

