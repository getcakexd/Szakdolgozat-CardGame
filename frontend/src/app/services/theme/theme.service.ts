import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark' | 'system';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private renderer: Renderer2;
  private themeMode = new BehaviorSubject<ThemeMode>('system');
  themeMode$ = this.themeMode.asObservable();

  private isDarkMode = new BehaviorSubject<boolean>(false);
  isDarkMode$ = this.isDarkMode.asObservable();

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);

    setTimeout(() => {
      const savedTheme = localStorage.getItem('themeMode');
      if (savedTheme && (savedTheme === 'light' || savedTheme === 'dark' || savedTheme === 'system')) {
        this.setTheme(savedTheme as ThemeMode);
      } else {
        this.setTheme('system');
      }

      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
      this.updateDarkModeStatus(prefersDark.matches);

      prefersDark.addEventListener('change', (e) => {
        if (this.themeMode.value === 'system') {
          this.updateDarkModeStatus(e.matches);
        }
      });
    }, 0);
  }

  setTheme(mode: ThemeMode): void {
    this.themeMode.next(mode);
    localStorage.setItem('themeMode', mode);

    this.renderer.removeClass(document.documentElement, 'light-theme');
    this.renderer.removeClass(document.documentElement, 'dark-theme');

    if (mode === 'light') {
      this.renderer.addClass(document.documentElement, 'light-theme');
      this.updateDarkModeStatus(false);
    } else if (mode === 'dark') {
      this.renderer.addClass(document.documentElement, 'dark-theme');
      this.updateDarkModeStatus(true);
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.updateDarkModeStatus(prefersDark);
    }
  }

  toggleTheme(): void {
    const currentMode = this.themeMode.value;
    if (currentMode === 'light') {
      this.setTheme('dark');
    } else if (currentMode === 'dark') {
      this.setTheme('system');
    } else {
      this.setTheme('light');
    }
  }

  private updateDarkModeStatus(isDark: boolean): void {
    this.isDarkMode.next(isDark);
  }
}
