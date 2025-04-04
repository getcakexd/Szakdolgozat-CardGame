import { Component, Inject } from "@angular/core"
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms"
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog"
import { User } from "../../models/user.model"
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {NgIf} from '@angular/common';
import {MatButton} from '@angular/material/button';

@Component({
  selector: "app-modify-user-dialog",
  templateUrl: "./modify-user-dialog.component.html",
  styleUrls: ["./modify-user-dialog.component.scss"],
  imports: [
    MatLabel,
    MatInput,
    NgIf,
    ReactiveFormsModule,
    MatError,
    MatFormField,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatDialogTitle
  ],
  standalone: true
})
export class ModifyUserDialogComponent {
  userForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ModifyUserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: User }
  ) {
    this.userForm = this.fb.group({
      username: [data.user.username, [Validators.required, Validators.minLength(3)]],
      email: [data.user.email, [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.dialogRef.close(this.userForm.value)
    }
  }

  onCancel(): void {
    this.dialogRef.close()
  }
}

