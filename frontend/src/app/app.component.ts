import { Component, OnInit } from '@angular/core';
import {Heartbeat} from './heartbeat';
import {HttpClient} from '@angular/common/http';
import {HeartbeatComponent} from './components/heartbeat/heartbeat.component';
import {NgForOf, NgIf} from '@angular/common';
import {UserListComponent} from './components/user-list/user-list.component';
import {LoginComponent} from './components/login/login.component';
import {SignupComponent} from './components/signup/signup.component';
import {MatToolbar} from '@angular/material/toolbar';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {MatButton} from '@angular/material/button';
import {User, UserService} from './services/user.service';
import {NavbarComponent} from './components/navbar/navbar.component';
import {RouterModule, RouterOutlet} from '@angular/router';
import {AppRoutingModule} from './app.routes';

@Component({
  selector: 'app-root',
  imports: [
    HeartbeatComponent,
    NgForOf, NgIf,
    UserListComponent, LoginComponent, SignupComponent,
    RouterModule,
    MatToolbar, MatMenu, MatMenuTrigger, MatMenuItem, MatButton, NavbarComponent, RouterOutlet,
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
    });
  }

  handleLogin(user: User) {
    this.userService.login(user).subscribe(() => {
      this.username = user.username; // Set the username after successful login
    });
  }

  logout() {
    this.userService.logout();  // Log the user out via the service
    this.username = null;
  }

}
