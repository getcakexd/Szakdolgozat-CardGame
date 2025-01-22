import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Heartbeat} from './heartbeat';
import {HttpClient} from '@angular/common/http';
import {HeartbeatComponent} from './heartbeat/heartbeat.component';
import {NgForOf, NgIf} from '@angular/common';
import {UserListComponent} from './components/user-list/user-list.component';
import {UserLoginComponent} from './components/user-login/user-login.component';
import {UserCreateComponent} from './components/user-create/user-create.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeartbeatComponent, NgForOf, UserListComponent, UserLoginComponent, UserCreateComponent, NgIf],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit{
  title = 'frontend';

  heartbeats : Heartbeat[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<Heartbeat[]>('http://localhost:4200/api/heartbeat')
      .subscribe(data => this.heartbeats = data);
  }

  isLoggedIn = false;
  username: string | null = null;

  handleLogin(username: string) {
    this.isLoggedIn = true;
    this.username = username;
  }

  logout() {
    this.isLoggedIn = false;
    this.username = null;
  }

}
