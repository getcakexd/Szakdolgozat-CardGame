import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ClubService} from '../../services/club/club.service';
import {Club} from '../../models/club.model';
import {NgForOf, NgIf} from '@angular/common';
import {ClubMember} from '../../models/club-member.model';
import {ClubMemberService} from '../../services/club-member/club-member.service';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-club',
  templateUrl: './club.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    FormsModule
  ],
  styleUrls: ['./club.component.css']
})
export class ClubComponent implements OnInit {
  public club: any = {};
  public members: ClubMember[] = [];
  protected userId: number = parseInt(localStorage.getItem('id') || '0');
  userRole: string = '';
  editingName = false;
  editingDescription = false;

  constructor(
    private route: ActivatedRoute,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const clubId = +params['id'];
      this.clubService.getClubById(clubId).subscribe({
        next: (clubData) => {
          this.club = clubData;
          this.loadMembers(clubId);
          this.getRole(clubId, this.userId);
        },
          error: (error) => {
          if (error.status === 404) {
            this.router.navigate(['/clubs']);
          } else {
            console.error('Error loading club:', error);
          }
        }
      });
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

  toggleEditName() {
    if (this.editingName) {
      this.saveClubChanges();
    }
    this.editingName = !this.editingName;
  }

  toggleEditDescription() {
    if (this.editingDescription) {
      this.saveClubChanges();
    }
    this.editingDescription = !this.editingDescription;
  }

  saveClubChanges() {
    this.clubService.updateClub(this.club.id, this.club.name, this.club.description, this.club.public).subscribe(() => {
    });
  }

  toggleVisibility() {
    this.club.public = !this.club.public;
    this.saveClubChanges();
  }

  deleteClub() {
    if (confirm('Are you sure you want to delete this club?')) {
      this.clubService.deleteClub(this.club.id).subscribe(() => {
        this.router.navigate(['/clubs']);
      });
    }
  }

  goBack() {
    this.router.navigate(['/clubs']);
  }
}
