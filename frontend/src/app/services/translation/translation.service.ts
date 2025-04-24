import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private currentLangSubject = new BehaviorSubject<string>('en');
  currentLang$ = this.currentLangSubject.asObservable();

  availableLanguages = [
    { code: 'en', name: 'English' },
    { code: 'hu', name: 'Magyar' },
  ];

  constructor(public translate: TranslateService) {
    const savedLang = localStorage.getItem('language') ||
      this.getBrowserLang() ||
      'en';

    this.setLanguage(savedLang);
  }

  private getBrowserLang(): string | null {
    const browserLang = this.translate.getBrowserLang();
    if (browserLang && this.availableLanguages.some(lang => lang.code === browserLang)) {
      return browserLang;
    }
    return null;
  }

  setLanguage(langCode: string): void {
    if (this.availableLanguages.some(lang => lang.code === langCode)) {
      this.translate.use(langCode);
      localStorage.setItem('language', langCode);
      this.currentLangSubject.next(langCode);
    }
  }

  getCurrentLang(): string {
    return this.currentLangSubject.value;
  }

  getLanguageName(code: string): string {
    const language = this.availableLanguages.find(lang => lang.code === code);
    return language ? language.name : code;
  }
}
