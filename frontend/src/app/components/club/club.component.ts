import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ClubService} from '../../services/club/club.service';
import {NgForOf, NgIf} from '@angular/common';
import {ClubMember} from '../../models/club-member.model';
import {ClubMemberService} from '../../services/club-member/club-member.service';
import {FormsModule} from '@angular/forms';
import {ClubInviteService} from '../../services/club-invite/club-invite.service';
import {ClubInvite} from '../../models/club-invite.model';
import {ClubChatComponent} from '../club-chat/club-chat.component';

@Component({
  selector: 'app-club',
  templateUrl: './club.component.html',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    FormsModule,
    ClubChatComponent
  ],
  styleUrls: ['./club.component.css']
})
export class ClubComponent implements OnInit {
  protected club: any = {};
  protected members: ClubMember[] = [];
  protected pendingInvites: any[] = [];
  protected inviteHistory: any[] = [];
  protected userId: number = parseInt(localStorage.getItem('id') || '0');
  inviteMessage: string = '';
  userRole: string = '';
  editingName = false;
  editingDescription = false;
  inviteUsername: any;
  showChat: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private clubInviteService: ClubInviteService,
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
          this.loadPendingInvites(clubId);
          this.loadInviteHistory(clubId);
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

  inviteUser(username: string) {
    this.clubInviteService.inviteUser(this.club.id, username).subscribe( {
      next: () => {
        this.inviteMessage = 'Invite sent';
      },
      error: (error) => {
        switch (error.status) {
          case 404:
            this.inviteMessage = 'User not found';
            break;
          case 409:
            this.inviteMessage = 'User is already a member';
            break;
          case 410:
            this.inviteMessage = 'User is already invited';
            break;
          default:
            this.inviteMessage = 'Error sending invite';
        }
      }
    });
  }

  loadPendingInvites(clubId: number) {
    this.clubInviteService.getPendingInvites(clubId).subscribe((invites) => {
      this.pendingInvites = invites;
    });
  }

  loadInviteHistory(clubId: number) {
    this.clubInviteService.getInviteHistory(clubId).subscribe((history) => {
      this.inviteHistory = history;
    });
  }

  cancelInvite(inviteId: number) {
    this.clubInviteService.cancelInvite(inviteId).subscribe(() => {
      window.location.reload();
    });
  }

  goBack() {
    this.router.navigate(['/clubs']);
  }

  toggleChat() {
    this.showChat = !this.showChat;
  }
}
