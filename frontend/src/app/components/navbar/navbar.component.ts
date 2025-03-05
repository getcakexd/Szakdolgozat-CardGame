import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import {NgIf} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [
    NgIf,
    RouterLink
  ],
  templateUrl: './navbar.component.html',
  standalone: true,
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit{
  isLoggedIn: boolean = false;

  constructor(protected userService: UserService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.userService.isLoggedIn();
  }

  protected readonly localStorage = localStorage;
}
