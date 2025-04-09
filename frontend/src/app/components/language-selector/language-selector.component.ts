import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { TranslationService } from '../../services/translation/translation.service';

@Component({
  selector: 'app-language-selector',
  templateUrl: './language-selector.component.html',
  styleUrls: ['./language-selector.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    MatButtonModule,
    MatMenuModule,
    MatIconModule
  ]
})
export class LanguageSelectorComponent {
  constructor(public translationService: TranslationService) {}

  changeLanguage(langCode: string): void {
    this.translationService.setLanguage(langCode);
  }
}
