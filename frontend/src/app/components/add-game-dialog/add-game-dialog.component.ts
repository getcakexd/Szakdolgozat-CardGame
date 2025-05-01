import {Component, ViewEncapsulation} from "@angular/core"
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from "@angular/forms"
import { MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog"
import { MatSlideToggle } from '@angular/material/slide-toggle';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { NgIf, NgFor } from '@angular/common';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatSelect, MatOption } from '@angular/material/select';
import { MatDivider } from '@angular/material/divider';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatCheckbox } from '@angular/material/checkbox';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: "app-add-game-dialog",
  templateUrl: "./add-game-dialog.component.html",
  styleUrls: ["./add-game-dialog.component.scss"],
  imports: [
    MatSlideToggle,
    ReactiveFormsModule,
    MatInput,
    NgIf,
    NgFor,
    MatError,
    MatLabel,
    MatFormField,
    MatDialogActions,
    MatButton,
    MatIconButton,
    MatIcon,
    MatSelect,
    MatOption,
    MatDivider,
    MatDialogContent,
    MatDialogTitle,
    MatCheckbox,
    TranslateModule,
    MarkdownModule
  ],
  encapsulation: ViewEncapsulation.None,
  standalone: true
})
export class AddGameDialogComponent {
  gameForm: FormGroup;
  availableLanguages = [
    { code: 'en', name: 'English' },
    { code: 'hu', name: 'Hungarian' },
  ];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddGameDialogComponent>,
    private translate: TranslateService
  ) {
    this.gameForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(3)]],
      minPlayers: [2, [Validators.required, Validators.min(1), Validators.max(10)]],
      maxPlayers: [4, [Validators.required, Validators.min(1), Validators.max(10)]],
      factorySign: ["", [Validators.required, Validators.minLength(3)]],
      factoryId: [0, [Validators.required, Validators.min(1)]],
      active: [true],
      descriptions: this.fb.array([this.createLocalizedContentGroup()]),
      rules: this.fb.array([this.createLocalizedContentGroup()])
    });
  }

  get descriptions(): FormArray {
    return this.gameForm.get('descriptions') as FormArray;
  }

  get rules(): FormArray {
    return this.gameForm.get('rules') as FormArray;
  }

  createLocalizedContentGroup(): FormGroup {
    return this.fb.group({
      language: ['en', Validators.required],
      content: ['', Validators.required],
      markdown: [true]
    });
  }

  addDescription(): void {
    this.descriptions.push(this.createLocalizedContentGroup());
  }

  removeDescription(index: number): void {
    if (this.descriptions.length > 1) {
      this.descriptions.removeAt(index);
    }
  }

  addRules(): void {
    this.rules.push(this.createLocalizedContentGroup());
  }

  removeRules(index: number): void {
    if (this.rules.length > 1) {
      this.rules.removeAt(index);
    }
  }

  getLanguageNameByCode(code: string): string {
    const language = this.availableLanguages.find(lang => lang.code === code);
    return language ? language.name : code;
  }

  getAvailableLanguagesForDescription(currentIndex: number): any[] {
    const usedLanguages = this.descriptions.controls
      .map((control, index) => index !== currentIndex ? control.get('language')?.value : null)
      .filter(lang => lang !== null);

    return this.availableLanguages.filter(lang => !usedLanguages.includes(lang.code));
  }

  getAvailableLanguagesForRules(currentIndex: number): any[] {
    const usedLanguages = this.rules.controls
      .map((control, index) => index !== currentIndex ? control.get('language')?.value : null)
      .filter(lang => lang !== null);

    return this.availableLanguages.filter(lang => !usedLanguages.includes(lang.code));
  }

  onSubmit(): void {
    if (this.gameForm.valid) {
      this.dialogRef.close(this.gameForm.value);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  previewMarkdown(content: string): string {
    return content || '';
  }
}
