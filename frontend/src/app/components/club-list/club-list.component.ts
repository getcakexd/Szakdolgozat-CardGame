import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ClubService } from '../../services/club/club.service';
import { Club } from '../../models/club.model';
import { ClubMemberService } from '../../services/club-member/club-member.service';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {MatTooltip} from '@angular/material/tooltip';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {IS_DEV} from '../../../environments/api-config';

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
    TranslateModule,
    MatTooltip,
    NgClass,
    MatProgressSpinner
  ],
  styleUrl: './club-list.component.scss'
})
export class ClubListComponent implements OnInit {
  @Input() isUserClubs = false
  @Output() clubClick = new EventEmitter<number>()

  protected clubs: Club[] = []
  private userId = 0
  isLoading = true
  memberCounts: Map<number, number> = new Map()

  constructor(
    private authService: AuthService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private translate: TranslateService,
    private snackBar: MatSnackBar,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
  }

  ngOnInit() {
    this.loadClubs()
  }

  loadClubs() {
    this.isLoading = true
    if (this.isUserClubs) {
      this.clubService.getClubsByUser(this.userId).subscribe({
        next: (data) => {
          this.clubs = data
          this.loadMemberCounts()
          this.isLoading = false
        },
        error: (error) => {
          this.snackBar.open(
            this.translate.instant("CLUBS.FAILED_LOAD_CLUBS"),
            this.translate.instant("COMMON.CLOSE"),
            { duration: 3000 },
          )
          this.isLoading = false
        },
      })
    } else {
      this.clubService.getJoinableClubs(this.userId).subscribe({
        next: (data) => {
          this.clubs = data
          this.loadMemberCounts()
          this.isLoading = false
        },
        error: (error) => {
          this.snackBar.open(
            this.translate.instant("CLUBS.FAILED_LOAD_CLUBS"),
            this.translate.instant("COMMON.CLOSE"),
            { duration: 3000 },
          )
          this.isLoading = false
        },
      })
    }
  }

  loadMemberCounts() {
    if (this.clubs.length === 0) return

    this.clubs.forEach((club) => {
      this.clubService.getMemberCount(club.id).subscribe({
        next: (response) => {
          this.memberCounts.set(club.id, response.memberCount)
          if (IS_DEV) console.log(`Member count for club ${club.id}:`, response.memberCount)
        },
        error: (error) => {
          console.error(`Error loading member count for club ${club.id}:`, error)
          this.memberCounts.set(club.id, 0)
        },
      })
    })
  }

  joinClub(event: Event, clubId: number) {
    event.stopPropagation()
    this.isLoading = true
    this.clubMemberService.addMember(clubId, this.userId).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant("CLUBS.JOIN_SUCCESS"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.loadClubs()
      },
      error: (error) => {
        this.snackBar.open(this.translate.instant("CLUBS.JOIN_FAILED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.isLoading = false
      },
    })
  }

  onClubClick(clubId: number) {
    this.clubClick.emit(clubId)
  }

  getMemberCount(club: Club): number {
    return this.memberCounts.get(club.id) || 0
  }
}
