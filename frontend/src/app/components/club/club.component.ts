import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubService } from '../../services/club/club.service';
import { ClubMember } from '../../models/club-member.model';
import { ClubMemberService } from '../../services/club-member/club-member.service';
import { FormsModule } from '@angular/forms';
import { ClubInviteService } from '../../services/club-invite/club-invite.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTabsModule } from '@angular/material/tabs';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth/auth.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ClubChatComponent } from '../club-chat/club-chat.component';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-club',
  templateUrl: './club.component.html',
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatTabsModule,
    MatListModule,
    MatDividerModule,
    MatChipsModule,
    MatBadgeModule,
    MatTooltipModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    TranslateModule,
    ClubChatComponent,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./club.component.css']
})
export class ClubComponent implements OnInit {
  protected club: any = {};
  protected members: ClubMember[] = [];
  protected pendingInvites: any[] = [];
  protected inviteHistory: any[] = [];
  protected userId: number = 0;
  inviteMessage: string = '';
  userRole: string = '';
  editingName = false;
  editingDescription = false;
  inviteUsername: string = '';
  showChat: boolean = false;
  isLoading: boolean = true;
  inviteError: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private clubInviteService: ClubInviteService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {
    this.userId = this.authService.getCurrentUserId() || 0;
    if (this.userId === 0) {
      this.router.navigate(['/login']).then(() => {
        window.location.reload();
      });
    }
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const clubId = +params['id'];
      this.loadClubData(clubId);
    });
  }

  loadClubData(clubId: number) {
    this.isLoading = true;
    this.clubService.getClubById(clubId).subscribe({
      next: (clubData) => {
        this.club = clubData;
        this.loadMembers(clubId);
        this.getRole(clubId, this.userId);
        this.loadPendingInvites(clubId);
        this.loadInviteHistory(clubId);
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 404) {
          this.snackBar.open(
            this.translate.instant('CLUB.CLUB_NOT_FOUND'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          this.router.navigate(['/clubs']);
        } else {
          this.snackBar.open(
            this.translate.instant('CLUB.ERROR_LOADING'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          console.error('Error loading club:', error);
        }
      }
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
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: this.translate.instant('COMMON.CONFIRM'),
          message: this.translate.instant('CLUB.CONFIRM_KICK', { username: member.username })
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.clubMemberService.kickMember(this.club.id, member.user.id).subscribe({
            next: () => {
              this.snackBar.open(
                this.translate.instant('CLUB.MEMBER_REMOVED', { username: member.username }),
                this.translate.instant('COMMON.CLOSE'),
                { duration: 3000 }
              );
              this.loadMembers(this.club.id);
            },
            error: (error) => {
              this.snackBar.open(
                this.translate.instant('CLUB.FAILED_REMOVE'),
                this.translate.instant('COMMON.CLOSE'),
                { duration: 3000 }
              );
            }
          });
        }
      });
    }
  }

  toggleModerator(member: ClubMember) {
    if (this.userRole === 'admin') {
      const newRole = member.role === 'moderator' ? 'member' : 'moderator';
      const action = member.role === 'moderator' ? 'demote' : 'promote';

      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: this.translate.instant('COMMON.CONFIRM'),
          message: this.translate.instant('CLUB.CONFIRM_ROLE', {
            action: action,
            username: member.username,
            role: newRole
          })
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.clubMemberService.updateMemberRole(this.club.id, member.user.id, newRole).subscribe({
            next: () => {
              this.snackBar.open(
                this.translate.instant('CLUB.ROLE_UPDATED', { username: member.username, role: newRole }),
                this.translate.instant('COMMON.CLOSE'),
                { duration: 3000 }
              );
              this.loadMembers(this.club.id);
            },
            error: (error) => {
              this.snackBar.open(
                this.translate.instant('CLUB.FAILED_ROLE_UPDATE', { action: action }),
                this.translate.instant('COMMON.CLOSE'),
                { duration: 3000 }
              );
            }
          });
        }
      });
    }
  }

  canToggleRole(member: ClubMember): boolean {
    return member.role === 'moderator' || member.role === 'member';
  }

  canKickMember(member: ClubMember): boolean {
    return (this.userRole === 'admin') ||
      (this.userRole === 'moderator' && member.role === 'member');
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
    this.clubService.updateClub(this.club.id, this.club.name, this.club.description, this.club.public).subscribe({
      next: () => {
        this.snackBar.open(
          this.translate.instant('CLUB.CLUB_UPDATED'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      },
      error: (error) => {
        this.snackBar.open(
          this.translate.instant('CLUB.FAILED_UPDATE'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  toggleVisibility() {
    const newVisibility = !this.club.public;
    const visibilityText = newVisibility ? 'public' : 'private';

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('CLUB.CONFIRM_VISIBILITY', { visibility: visibilityText })
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.club.public = newVisibility;
        this.saveClubChanges();
      }
    });
  }

  deleteClub() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('CLUB.DELETE_CLUB'),
        message: this.translate.instant('CLUB.CONFIRM_DELETE')
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.clubService.deleteClub(this.club.id).subscribe({
          next: () => {
            this.snackBar.open(
              this.translate.instant('CLUB.CLUB_DELETED'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
            this.router.navigate(['/clubs']);
          },
          error: (error) => {
            this.snackBar.open(
              this.translate.instant('CLUB.FAILED_DELETE'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
          }
        });
      }
    });
  }

  inviteUser(username: string): void {
    if (!username || username.trim() === '') {
      this.inviteMessage = this.translate.instant('CLUB.ENTER_USERNAME_ERROR');
      this.inviteError = true;
      return;
    }

    this.inviteMessage = '';
    this.inviteError = false;

    this.clubInviteService.inviteUser(this.club.id, username).subscribe({
      next: (response) => {
        this.inviteUsername = '';
        this.inviteMessage = this.translate.instant('CLUB.INVITE_SENT');
        this.inviteError = false;
        this.loadPendingInvites(this.club.id);
      },
      error: (error) => {
        this.inviteMessage = error.error?.message || this.translate.instant('CLUB.FAILED_INVITE');
        this.inviteError = true;
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
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('CLUB.CONFIRM_CANCEL_INVITE')
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.clubInviteService.cancelInvite(inviteId).subscribe({
          next: () => {
            this.snackBar.open(
              this.translate.instant('CLUB.INVITE_CANCELLED'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
            this.loadPendingInvites(this.club.id);
          },
          error: (error) => {
            this.snackBar.open(
              this.translate.instant('CLUB.FAILED_CANCEL'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
          }
        });
      }
    });
  }

  leaveClub() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant('CLUB.LEAVE_CLUB'),
        message: this.translate.instant('CLUB.CONFIRM_LEAVE')
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.clubMemberService.leaveClub(this.club.id, this.userId).subscribe({
          next: () => {
            this.snackBar.open(
              this.translate.instant('CLUB.LEFT_CLUB'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
            this.router.navigate(['/clubs']);
          },
          error: (error) => {
            this.snackBar.open(
              error.error?.message || this.translate.instant('CLUB.FAILED_LEAVE'),
              this.translate.instant('COMMON.CLOSE'),
              { duration: 3000 }
            );
          }
        });
      }
    });
  }

  goBack() {
    this.router.navigate(['/clubs']);
  }

  toggleChat() {
    this.showChat = !this.showChat;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'accepted': return 'primary';
      case 'pending': return 'accent';
      case 'declined': return 'warn';
      case 'cancelled': return 'warn';
      default: return '';
    }
  }

  getRoleColor(role: string): string {
    switch (role) {
      case 'admin': return 'primary';
      case 'moderator': return 'accent';
      case 'member': return 'default';
      default: return '';
    }
  }
}
