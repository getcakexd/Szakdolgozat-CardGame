import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent, MatDialogModule,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatButton, MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <mat-dialog-content>{{data.message}}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">Confirm</button>
    </mat-dialog-actions>
  `,
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatDialogTitle, MatDialogContent, MatDialogActions, MatButton, MatDialogClose]
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: {title: string, message: string}) {}
}
