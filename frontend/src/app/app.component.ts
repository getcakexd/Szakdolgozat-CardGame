import { Component, OnInit } from '@angular/core';
import {UserService} from './services/user/user.service';
import {NavbarComponent} from './components/navbar/navbar.component';
import {RouterModule, RouterOutlet} from '@angular/router';

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
  isLoggedIn = false;
  username: string | null = null;

  constructor(private userService : UserService) {}

  ngOnInit(): void {
    this.userService.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;
      if (status) {
        this.username = localStorage.getItem('username');
      } else {
        this.username = null;
      }
    });
  }
}
