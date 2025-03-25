import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ClubService} from '../../services/club/club.service';
import {Club} from '../../models/club.model';
import {NgForOf, NgIf} from '@angular/common';
import {FriendRequestService} from '../../services/friend-request/friend-request.service';
import {ClubMember} from '../../models/club-member.model';
import {ClubMemberService} from '../../services/club-member/club-member.service';

@Component({
  selector: 'app-club',
  templateUrl: './club.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  styleUrls: ['./club.component.css']
})
export class ClubComponent implements OnInit {
  // @ts-ignore
  public club: Club;
  public members: ClubMember[] = [];
  protected userId: number = parseInt(localStorage.getItem('id') || '0');
  userRole: string = '';

  constructor(
    private route: ActivatedRoute,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const clubId = +params['id'];
      this.loadClub(clubId);
      this.loadMembers(clubId);
      this.getRole(clubId, this.userId);
    });
  }

  loadClub(clubId: number) {
    this.clubService.getClubById(clubId).subscribe((club: Club) => {
      this.club = club;
    });
  }

  loadMembers(clubId: number) {
    this.clubMemberService.getMembers(clubId).subscribe(
      (members: ClubMember[]) => {
        this.members = members;
      }
    );
  }

  getRole(clubId: number, userId: number) {
    return this.clubMemberService.getUserRole(clubId, userId).subscribe(
      (role) => {
        this.userRole = role.role;
      }
    );
  }

  kickMember(member: ClubMember) {
    if (this.userRole === 'admin' || (this.userRole === 'moderator' && member.role === 'member')) {
      this.clubMemberService.kickMember(this.club.id, member.user.id).subscribe(() => {
        this.loadMembers(this.club.id);
      });
    }
  }

  toggleModerator(member: ClubMember) {
    if (this.userRole === 'admin') {
      const newRole = member.role === 'moderator' ? 'member' : 'moderator';

      this.clubMemberService.updateMemberRole(this.club.id, member.user.id, newRole).subscribe(() => {
        this.loadMembers(this.club.id);
      });
    }
  }

  goBack() {
    this.router.navigate(['/clubs']);
  }
}
