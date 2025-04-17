import {Component, OnInit} from '@angular/core';
import {NavbarComponent} from './components/navbar/navbar.component';
import {RouterModule, RouterOutlet} from '@angular/router';
import {AuthService} from './services/auth/auth.service';
import {AsyncPipe, NgClass} from '@angular/common';
import {Observable} from 'rxjs';
import {ThemeService} from './services/theme/theme.service';

@Component({
  selector: 'app-root',
  imports: [
    NavbarComponent,
    RouterOutlet, RouterModule, NgClass, AsyncPipe,
  ],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  isDarkMode$: Observable<boolean>;
  title = 'frontend';
  username: string | null = null;

  constructor(
    private authService : AuthService,
    private themeService: ThemeService
  ) {
    this.isDarkMode$ = this.themeService.isDarkMode$;
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      if (user !== null) {
        this.username = user.username;
      } else {
        this.username = null;
      }
    });
  }
}
