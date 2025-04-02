import { Component } from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {NgIf} from '@angular/common';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-lobby-create-dialog',
  template: `
    <h2 mat-dialog-title>Create Lobby</h2>
    <mat-dialog-content>
      <form [formGroup]="lobbyForm" class="lobby-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Lobby Name</mat-label>
          <input matInput formControlName="name" required>
          <mat-error *ngIf="lobbyForm.get('name')?.hasError('required')">
            Lobby name is required
          </mat-error>
        </mat-form-field>

        <div class="form-row">
          <mat-slide-toggle formControlName="isPublic">
            Public Lobby
          </mat-slide-toggle>
        </div>

        <div class="form-row">
          <mat-slide-toggle formControlName="withPoints">
            Play with Points
          </mat-slide-toggle>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Game Mode</mat-label>
          <mat-select formControlName="gameMode">
            <mat-option value="classic">Classic</mat-option>
            <mat-option value="advanced">Advanced</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Max Players</mat-label>
          <mat-select formControlName="maxPlayers">
            <mat-option [value]="2">2 Players</mat-option>
            <mat-option [value]="3">3 Players</mat-option>
            <mat-option [value]="4">4 Players</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" [disabled]="lobbyForm.invalid" (click)="onSubmit()">Create</button>
    </mat-dialog-actions>
  `,
  standalone: true,
  imports: [
    MatDialogTitle,
    MatError,
    MatLabel,
    MatFormField,
    MatInput,
    FormsModule,
    NgIf,
    ReactiveFormsModule,
    MatSlideToggle,
    MatOption,
    MatSelect,
    MatDialogActions,
    MatDialogContent,
    MatButton
  ],
  styles: [`
    .lobby-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .full-width {
      width: 100%;
    }

    .form-row {
      margin: 8px 0;
    }
  `]
})
export class LobbyCreateDialogComponent {
  lobbyForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<LobbyCreateDialogComponent>,
    private fb: FormBuilder
  ) {
    this.lobbyForm = this.fb.group({
      name: ['', Validators.required],
      isPublic: [false],
      withPoints: [true],
      gameMode: ['classic'],
      maxPlayers: [4]
    });
  }

  onSubmit(): void {
    if (this.lobbyForm.valid) {
      this.dialogRef.close(this.lobbyForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
