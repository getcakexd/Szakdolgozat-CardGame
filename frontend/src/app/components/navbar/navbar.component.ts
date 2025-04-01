import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user/user.service';
import { NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [NgIf, RouterLink, MatToolbar, MatButton],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isLoggedIn: boolean = false;

  constructor(protected userService: UserService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.userService.isLoggedIn();
  }

  protected readonly localStorage = localStorage;
}
