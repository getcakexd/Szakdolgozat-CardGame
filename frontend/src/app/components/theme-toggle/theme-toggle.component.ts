import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Observable, Subscription } from 'rxjs';
import { ThemeMode, ThemeService } from '../../services/theme/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <button
      mat-icon-button
      (click)="toggleTheme()"
      aria-label="Toggle theme"
      [matTooltip]="getTooltipText()">
      <mat-icon>{{ getThemeIcon() }}</mat-icon>
    </button>
  `,
  styles: [`
    button {
      transition: transform 0.3s ease;
    }
    button:active {
      transform: rotate(30deg);
    }
  `]
})
export class ThemeToggleComponent implements OnInit, OnDestroy {
  isDarkMode$!: Observable<boolean>;
  currentTheme: ThemeMode = 'system';
  private themeSubscription!: Subscription;

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.isDarkMode$ = this.themeService.isDarkMode$;
    this.themeSubscription = this.themeService.themeMode$.subscribe(theme => {
      this.currentTheme = theme;
    });
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) {
      this.themeSubscription.unsubscribe();
    }
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  getThemeIcon(): string {
    if (this.currentTheme === 'light') {
      return 'light_mode';
    } else if (this.currentTheme === 'dark') {
      return 'dark_mode';
    } else {
      return this.themeService.getCurrentTheme() ? 'dark_mode' : 'light_mode';
    }
  }

  getTooltipText(): string {
    switch (this.currentTheme) {
      case 'light':
        return 'Light mode (click to toggle)';
      case 'dark':
        return 'Dark mode (click to toggle)';
      case 'system':
        return 'System theme (click to toggle)';
      default:
        return 'Toggle theme';
    }
  }
}
