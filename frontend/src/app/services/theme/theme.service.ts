import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private darkMode = new BehaviorSubject<boolean>(this.getInitialThemePreference());
  public darkMode$ = this.darkMode.asObservable();

  constructor() {
    this.initTheme();
  }

  private getInitialThemePreference(): boolean {
    const savedPreference = localStorage.getItem('darkMode');
    if (savedPreference) {
      return savedPreference === 'true';
    }

    return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }

  private initTheme(): void {
    this.setTheme(this.darkMode.value);

    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
        if (localStorage.getItem('darkMode') === null) {
          this.setTheme(e.matches);
        }
      });
    }
  }

  public toggleDarkMode(): void {
    this.setTheme(!this.darkMode.value);
  }

  private setTheme(isDark: boolean): void {
    this.darkMode.next(isDark);
    localStorage.setItem('darkMode', isDark.toString());

    if (isDark) {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }
}
