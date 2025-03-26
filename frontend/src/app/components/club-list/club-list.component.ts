import {Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import {ClubService} from '../../services/club/club.service';
import {Club} from '../../models/club.model';
import {ClubMemberService} from '../../services/club-member/club-member.service';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  styleUrl: './club-list.component.css'
})
export class ClubListComponent implements OnInit {
  @Input() isUserClubs: boolean = false;
  @Output() clubClick = new EventEmitter<number>();

  protected clubs: Club[] = [];
  private userId: number = parseInt(localStorage.getItem('id') || '');

  constructor(private clubService: ClubService, private clubMemberService: ClubMemberService) {}

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
