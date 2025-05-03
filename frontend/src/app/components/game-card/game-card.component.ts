import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TranslateModule } from '@ngx-translate/core';
import { Game } from '../../models/game.model';
import { TranslationService } from '../../services/translation/translation.service';
import { MatChip, MatChipSet } from '@angular/material/chips';
import { DomSanitizer } from '@angular/platform-browser';
import {MarkdownComponent} from 'ngx-markdown';

@Component({
  selector: 'app-game-card',
  templateUrl: './game-card.component.html',
  styleUrls: ['./game-card.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatSlideToggleModule,
    TranslateModule,
    MatChip,
    MatChipSet,
    MarkdownComponent,
  ]
})
export class GameCardComponent {
  @Input() game!: Game
  @Input() showRulesButton = true
  @Input() showActiveToggle = false
  @Input() isAdmin = false

  @Output() toggleActive = new EventEmitter<{ game: Game; active: boolean }>()
  @Output() deleteGame = new EventEmitter<Game>()
  @Output() editGame = new EventEmitter<Game>()

  showRules = false
  currentLanguage: string

  constructor(
    private translationService: TranslationService,
    private sanitizer: DomSanitizer,
  ) {
    this.currentLanguage = this.translationService.getCurrentLang()
    this.translationService.currentLang$.subscribe((lang) => {
      this.currentLanguage = lang
    })
  }

  getDescriptionInCurrentLanguage(): string {
    if (!this.game.descriptions || this.game.descriptions.length === 0) {
      return ""
    }

    const description = this.game.descriptions.find((desc) => desc.language === this.currentLanguage)
    return description ? description.content : this.game.descriptions[0].content
  }

  getRulesInCurrentLanguage(): string {
    if (!this.game.rules || this.game.rules.length === 0) {
      return ""
    }

    const rules = this.game.rules.find((rule) => rule.language === this.currentLanguage)
    return rules ? rules.content : this.game.rules[0].content
  }

  isDescriptionMarkdown(): boolean {
    if (!this.game.descriptions || this.game.descriptions.length === 0) {
      return false
    }

    const description = this.game.descriptions.find((desc) => desc.language === this.currentLanguage)
    return description ? !!description.markdown : !!this.game.descriptions[0].markdown
  }

  isRulesMarkdown(): boolean {
    if (!this.game.rules || this.game.rules.length === 0) {
      return false
    }

    const rules = this.game.rules.find((rule) => rule.language === this.currentLanguage)
    return rules ? !!rules.markdown : !!this.game.rules[0].markdown
  }

  toggleRules(): void {
    this.showRules = !this.showRules
  }

  onToggleActive(event: any): void {
    this.toggleActive.emit({
      game: this.game,
      active: event.checked,
    })
  }

  onDeleteGame(): void {
    this.deleteGame.emit(this.game)
  }

  onEditGame(): void {
    this.editGame.emit(this.game)
  }

  getAvailableLanguages(): string[] {
    const descriptionLanguages = this.game.descriptions ? this.game.descriptions.map((desc) => desc.language) : []
    const rulesLanguages = this.game.rules ? this.game.rules.map((rule) => rule.language) : []

    return [...new Set([...descriptionLanguages, ...rulesLanguages])]
  }
}
