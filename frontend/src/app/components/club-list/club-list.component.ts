import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ClubService } from '../../services/club/club.service';
import { Club } from '../../models/club.model';
import { ClubMemberService } from '../../services/club-member/club-member.service';
import { NgForOf, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    MatCardModule,
    MatButtonModule,
    MatListModule,
    MatIconModule,
    MatDividerModule,
    TranslateModule
  ],
  styleUrl: './club-list.component.css'
})
export class ClubListComponent implements OnInit {
  @Input() isUserClubs: boolean = false;
  @Output() clubClick = new EventEmitter<number>();

  protected clubs: Club[] = [];
  private userId: number = 0;

  constructor(
    private authService: AuthService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private translate: TranslateService
  ) {
    this.userId = this.authService.getCurrentUserId() || 0;
  }

  ngOnInit() {
    this.LoadClubs();
  }

  LoadClubs() {
    if (this.isUserClubs) {
      this.clubService.getClubsByUser(this.userId).subscribe((data) => {
        this.clubs = data;
      });
    } else {
      this.clubService.getJoinableClubs(this.userId).subscribe((data) => {
        this.clubs = data;
      });
    }
  }

  JoinClub(clubId: number) {
    this.clubMemberService.addMember(clubId, this.userId).subscribe(() => {
      this.LoadClubs();
      window.location.reload();
    });
  }

  onClubClick(clubId: number) {
    this.clubClick.emit(clubId);
  }
}
