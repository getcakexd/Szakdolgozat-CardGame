import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ThemeService } from '../../services/theme/theme.service';
import { MatIcon } from '@angular/material/icon';
import { AsyncPipe, NgIf } from '@angular/common';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIconButton } from '@angular/material/button';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-theme-toggle',
  templateUrl: './theme-toggle.component.html',
  standalone: true,
  imports: [
    MatIcon,
    NgIf,
    AsyncPipe,
    MatTooltip,
    MatIconButton,
    TranslateModule
  ],
  styleUrls: ['./theme-toggle.component.scss']
})
export class ThemeToggleComponent implements OnInit {
  isDarkMode$: Observable<boolean>;

  constructor(
    private themeService: ThemeService,
    private translateService: TranslateService
  ) {
    this.isDarkMode$ = this.themeService.darkMode$;
  }

  ngOnInit(): void {}

  toggleTheme(): void {
    this.themeService.toggleDarkMode();
  }
}
