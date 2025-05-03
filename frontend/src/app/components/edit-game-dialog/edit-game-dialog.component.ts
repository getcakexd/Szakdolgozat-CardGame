import { Component, Inject, ViewEncapsulation } from "@angular/core"
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from "@angular/forms"
import {
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog"
import { MatSlideToggle } from "@angular/material/slide-toggle"
import { MatError, MatFormField, MatInput, MatLabel } from "@angular/material/input"
import { NgIf, NgFor } from "@angular/common"
import { MatButton, MatIconButton } from "@angular/material/button"
import { MatIcon } from "@angular/material/icon"
import { MatSelect, MatOption } from "@angular/material/select"
import { MatDivider } from "@angular/material/divider"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { MatCheckbox } from "@angular/material/checkbox"
import { MarkdownModule } from "ngx-markdown"
import { Game } from "../../models/game.model"

@Component({
  selector: "app-edit-game-dialog",
  templateUrl: "./edit-game-dialog.component.html",
  styleUrls: ["./edit-game-dialog.component.scss"],
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
    MarkdownModule,
  ],
  encapsulation: ViewEncapsulation.None,
  standalone: true,
})
export class EditGameDialogComponent {
  gameForm: FormGroup
  availableLanguages = [
    { code: "en", name: "English" },
    { code: "hu", name: "Hungarian" },
  ]
  gameId!: number;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EditGameDialogComponent>,
    private translate: TranslateService,
    @Inject(MAT_DIALOG_DATA) public data: { game: Game }
  ) {
    this.gameId = data.game.id;
    this.gameForm = this.fb.group({
      name: [data.game.name, [Validators.required, Validators.minLength(3)]],
      minPlayers: [data.game.minPlayers, [Validators.required, Validators.min(1), Validators.max(10)]],
      maxPlayers: [data.game.maxPlayers, [Validators.required, Validators.min(1), Validators.max(10)]],
      factorySign: [data.game.factorySign, [Validators.required, Validators.minLength(3)]],
      factoryId: [data.game.factoryId, [Validators.required, Validators.min(1)]],
      active: [data.game.active],
      descriptions: this.fb.array([]),
      rules: this.fb.array([])
    });

    if (data.game.descriptions && data.game.descriptions.length > 0) {
      data.game.descriptions.forEach(desc => {
        this.descriptions.push(this.createLocalizedContentGroup(desc.language, desc.content, desc.markdown));
      });
    } else {
      this.descriptions.push(this.createLocalizedContentGroup());
    }

    if (data.game.rules && data.game.rules.length > 0) {
      data.game.rules.forEach(rule => {
        this.rules.push(this.createLocalizedContentGroup(rule.language, rule.content, rule.markdown));
      });
    } else {
      this.rules.push(this.createLocalizedContentGroup());
    }
  }

  get descriptions(): FormArray {
    return this.gameForm.get("descriptions") as FormArray
  }

  get rules(): FormArray {
    return this.gameForm.get("rules") as FormArray
  }

  createLocalizedContentGroup(language = "en", content = "", markdown = true): FormGroup {
    return this.fb.group({
      language: [language, Validators.required],
      content: [content, Validators.required],
      markdown: [markdown],
    })
  }

  addDescription(): void {
    this.descriptions.push(this.createLocalizedContentGroup())
  }

  removeDescription(index: number): void {
    if (this.descriptions.length > 1) {
      this.descriptions.removeAt(index)
    }
  }

  addRules(): void {
    this.rules.push(this.createLocalizedContentGroup())
  }

  removeRules(index: number): void {
    if (this.rules.length > 1) {
      this.rules.removeAt(index)
    }
  }

  getLanguageNameByCode(code: string): string {
    const language = this.availableLanguages.find((lang) => lang.code === code)
    return language ? language.name : code
  }

  getAvailableLanguagesForDescription(currentIndex: number): any[] {
    const usedLanguages = this.descriptions.controls
      .map((control, index) => (index !== currentIndex ? control.get("language")?.value : null))
      .filter((lang) => lang !== null)

    return this.availableLanguages.filter((lang) => !usedLanguages.includes(lang.code))
  }

  getAvailableLanguagesForRules(currentIndex: number): any[] {
    const usedLanguages = this.rules.controls
      .map((control, index) => (index !== currentIndex ? control.get("language")?.value : null))
      .filter((lang) => lang !== null)

    return this.availableLanguages.filter((lang) => !usedLanguages.includes(lang.code))
  }

  onSubmit(): void {
    if (this.gameForm.valid) {
      const formValue = this.gameForm.value
      formValue.id = this.gameId
      this.dialogRef.close(formValue)
    }
  }

  onCancel(): void {
    this.dialogRef.close()
  }

  previewMarkdown(content: string): string {
    return content || ""
  }
}
