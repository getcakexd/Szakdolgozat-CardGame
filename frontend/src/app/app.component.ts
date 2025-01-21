import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {Heartbeat} from './heartbeat';
import {HttpClient} from '@angular/common/http';
import {HeartbeatComponent} from './heartbeat/heartbeat.component';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeartbeatComponent, NgForOf],
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

}
