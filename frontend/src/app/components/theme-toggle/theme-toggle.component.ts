import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Observable } from 'rxjs';
import {ThemeMode, ThemeService} from '../../services/theme/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatMenuModule],
  template: `
    <button mat-icon-button [matMenuTriggerFor]="themeMenu" aria-label="Theme selector">
      <mat-icon>{{ (isDarkMode$ | async) ? 'dark_mode' : 'light_mode' }}</mat-icon>
    </button>
    <mat-menu #themeMenu="matMenu">
      <button mat-menu-item (click)="setTheme('light')">
        <mat-icon>light_mode</mat-icon>
        <span>Light</span>
      </button>
      <button mat-menu-item (click)="setTheme('dark')">
        <mat-icon>dark_mode</mat-icon>
        <span>Dark</span>
      </button>
      <button mat-menu-item (click)="setTheme('system')">
        <mat-icon>settings_suggest</mat-icon>
        <span>System</span>
      </button>
    </mat-menu>
  `
})
export class ThemeToggleComponent implements OnInit {
  isDarkMode$!: Observable<boolean>;

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.isDarkMode$ = this.themeService.isDarkMode$;
  }

  setTheme(mode: 'light' | 'dark' | 'system'): void {
    this.themeService.setTheme(mode as ThemeMode);
  }
}
