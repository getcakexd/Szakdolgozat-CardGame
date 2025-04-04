import { Component, OnInit } from '@angular/core';
import {UserService} from './services/user/user.service';
import {NavbarComponent} from './components/navbar/navbar.component';
import {RouterModule, RouterOutlet} from '@angular/router';
import {AuthService} from './services/auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [
    NavbarComponent,
    RouterOutlet, RouterModule,
  ],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'frontend';
  username: string | null = null;

  constructor(
    private authService : AuthService
  ) {}

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
