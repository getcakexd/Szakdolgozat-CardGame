import { Component, OnInit } from "@angular/core";
import { MatTabsModule } from "@angular/material/tabs";
import { MatCardModule } from "@angular/material/card";
import { TranslateModule } from '@ngx-translate/core';
import { RouterModule } from "@angular/router";
import { AuthService } from "../../services/auth/auth.service";
import { NgIf } from "@angular/common";
import {CreateTicketComponent} from '../../components/create-ticket/create-ticket.component';
import {TicketListComponent} from '../../components/ticket-list/ticket-list.component';

@Component({
  selector: "app-support",
  templateUrl: "./support.component.html",
  styleUrls: ["./support.component.scss"],
  standalone: true,
  imports: [
    MatTabsModule,
    MatCardModule,
    TranslateModule,
    CreateTicketComponent,
    TicketListComponent,
    RouterModule,
    NgIf
  ],
})
export class SupportComponent implements OnInit {
  isLoggedIn = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
  }
}
