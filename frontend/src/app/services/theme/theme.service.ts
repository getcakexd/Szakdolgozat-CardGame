import { Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark' | 'system';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private renderer: Renderer2;
  private themeMode = new BehaviorSubject<'light' | 'dark' | 'system'>('system');
  themeMode$ = this.themeMode.asObservable();

  private themePalette = new BehaviorSubject<string>('blue-palette');
  themePalette$ = this.themePalette.asObservable();

  private isDarkMode = new BehaviorSubject<boolean>(false);
  isDarkMode$ = this.isDarkMode.asObservable();

  constructor(rendererFactory: RendererFactory2) {
    this.renderer = rendererFactory.createRenderer(null, null);

    setTimeout(() => {
      const savedTheme = localStorage.getItem('themeMode');
      if (savedTheme && (savedTheme === 'light' || savedTheme === 'dark' || savedTheme === 'system')) {
        this.setTheme(savedTheme as 'light' | 'dark' | 'system');
      } else {
        this.setTheme('system');
      }

      const savedPalette = localStorage.getItem('themePalette');
      if (savedPalette) {
        this.setPalette(savedPalette);
      } else {
        this.setPalette('blue-palette');
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

  setTheme(mode: 'light' | 'dark' | 'system'): void {
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

  setPalette(palette: string): void {
    this.themePalette.next(palette);
    localStorage.setItem('themePalette', palette);

    document.documentElement.setAttribute('data-theme-palette', palette);
  }

  applyPaletteChanges(): void {
    window.location.reload();
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
