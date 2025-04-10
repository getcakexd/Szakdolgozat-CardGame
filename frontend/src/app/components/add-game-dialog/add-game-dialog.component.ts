import { Component } from "@angular/core"
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms"
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog"
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { NgIf } from '@angular/common';
import { MatButton } from '@angular/material/button';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-add-game-dialog",
  templateUrl: "./add-game-dialog.component.html",
  styleUrls: ["./add-game-dialog.component.scss"],
  imports: [
    MatSlideToggle,
    ReactiveFormsModule,
    MatInput,
    NgIf,
    MatError,
    MatLabel,
    MatFormField,
    MatDialogActions,
    MatButton,
    MatDialogContent,
    MatDialogTitle,
    TranslateModule
  ],
  standalone: true
})
export class AddGameDialogComponent {
  gameForm: FormGroup

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddGameDialogComponent>,
    private translate: TranslateService
  ) {
    this.gameForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(3)]],
      description: ["", Validators.required],
      active: [true],
    })
  }

  onSubmit(): void {
    if (this.gameForm.valid) {
      this.dialogRef.close(this.gameForm.value)
    }
  }

  onCancel(): void {
    this.dialogRef.close()
  }
}
