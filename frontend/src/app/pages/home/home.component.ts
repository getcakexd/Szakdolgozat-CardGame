import {Component, OnInit} from '@angular/core';
import {NgIf} from '@angular/common';
import {AuthService} from '../../services/auth/auth.service';

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

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.username = this.authService.currentUser?.username || null;
  }
}
