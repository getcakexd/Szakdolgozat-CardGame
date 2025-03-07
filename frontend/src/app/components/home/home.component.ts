import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {UserService} from '../../services/friends/user.service';

@Component({
  selector: 'app-home',
  imports: [
    NgIf
  ],
  templateUrl: './home.component.html',
  standalone: true,
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  username: string | null = null;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.username = this.userService.getLoggedInUsername();
  }
}
