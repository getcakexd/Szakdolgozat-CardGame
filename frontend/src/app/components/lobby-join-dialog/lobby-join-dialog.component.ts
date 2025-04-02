import { Component } from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {NgIf} from '@angular/common';
import {MatInput} from '@angular/material/input';
import {MatButton} from '@angular/material/button';


@Component({
  selector: 'app-lobby-join-dialog',
  template: `
    <h2 mat-dialog-title>Join Lobby</h2>
    <mat-dialog-content>
      <form [formGroup]="joinForm" class="join-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Lobby Code</mat-label>
          <input matInput formControlName="code" required>
          <mat-hint>Enter the 6-character lobby code</mat-hint>
          <mat-error *ngIf="joinForm.get('code')?.hasError('required')">
            Lobby code is required
          </mat-error>
          <mat-error *ngIf="joinForm.get('code')?.hasError('minlength') || joinForm.get('code')?.hasError('maxlength')">
            Lobby code must be 6 characters
          </mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" [disabled]="joinForm.invalid" (click)="onSubmit()">Join</button>
    </mat-dialog-actions>
  `,
  standalone: true,
  imports: [
    MatError,
    NgIf,
    MatInput,
    FormsModule,
    MatFormField,
    ReactiveFormsModule,
    MatDialogActions,
    MatButton,
    MatDialogContent,
    MatDialogTitle,
    MatLabel,
    MatHint,
    MatError,
    NgIf,
    MatFormField,
    MatInput,
    MatButton
  ],
  styles: [`
    .join-form {
      display: flex;
      flex-direction: column;
    }

    .full-width {
      width: 100%;
    }
  `]
})
export class LobbyJoinDialogComponent {
  joinForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LobbyJoinDialogComponent>,
    private fb: FormBuilder
  ) {
    this.joinForm = this.fb.group({
      code: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(6)
      ]]
    });
  }

  onSubmit(): void {
    if (this.joinForm.valid) {
      this.dialogRef.close(this.joinForm.get('code')?.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
