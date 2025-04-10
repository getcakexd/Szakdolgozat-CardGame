import { Component } from "@angular/core"
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from "@angular/forms"
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog"
import { MatIcon } from '@angular/material/icon';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { NgForOf, NgIf } from '@angular/common';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatInput } from '@angular/material/input';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-add-user-dialog",
  templateUrl: "./add-user-dialog.component.html",
  styleUrls: ["./add-user-dialog.component.scss"],
  imports: [
    MatDialogTitle,
    MatIcon,
    MatError,
    MatLabel,
    NgIf,
    MatFormField,
    FormsModule,
    MatSelect,
    MatOption,
    NgForOf,
    MatIconButton,
    MatInput,
    MatDialogContent,
    ReactiveFormsModule,
    MatDialogActions,
    MatButton,
    TranslateModule
  ],
  standalone: true
})
export class AddUserDialogComponent {
  userForm: FormGroup
  roles: string[] = ["ROLE_USER", "ROLE_AGENT", "ROLE_ADMIN"]
  hidePassword = true

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddUserDialogComponent>,
    private translate: TranslateService
  ) {
    this.userForm = this.fb.group({
      username: ["", [Validators.required, Validators.minLength(3)]],
      email: ["", [Validators.required, Validators.email]],
      password: ["", [Validators.required, Validators.minLength(6)]],
      role: ["ROLE_USER", Validators.required],
    })
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
