import { Component, OnInit } from '@angular/core';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import {AuthService} from '../../services/auth/auth.service';
import {ThemeToggleComponent} from '../theme-toggle/theme-toggle.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    NgIf,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    ThemeToggleComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLoggedIn: boolean = false;
  username: string = '';
  isAdmin: boolean = false;
  isAgent: boolean = false;
  isRoot: boolean = false;

  constructor(protected authService: AuthService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.username = this.authService.currentUser?.username || '';
    this.isAdmin = this.authService.isAdmin();
    this.isAgent = this.authService.isAgent();
    this.isRoot = this.authService.isRoot();
  }

  logout(): void {
    this.authService.logout();
  }
}
