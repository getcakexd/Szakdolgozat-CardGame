import { Component, OnInit } from '@angular/core';
import { ClubCreateComponent } from '../../components/club-create/club-create.component';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import { ClubListComponent } from '../../components/club-list/club-list.component';
import { Router } from '@angular/router';
import { ClubInviteService } from '../../services/club-invite/club-invite.service';
import { ClubInvite } from '../../models/club-invite.model';
import { UserService } from '../../services/user/user.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import {ClubGameStats} from '../../models/club-stats.model';
import {IS_DEV} from '../../../environments/api-config';
import {ClubService} from '../../services/club/club.service';
import {ClubMemberService} from '../../services/club-member/club-member.service';
import {StatsService} from '../../services/stats/stats.service';
import {Game} from '../../models/game.model';
import {Club} from '../../models/club.model';
import {MatTooltip} from '@angular/material/tooltip';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatOption} from '@angular/material/core';
import {MatFormField, MatLabel, MatSelect} from '@angular/material/select';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-club-page',
  imports: [
    ClubCreateComponent,
    NgIf,
    ClubListComponent,
    NgForOf,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatExpansionModule,
    MatBadgeModule,
    TranslateModule,
    MatTooltip,
    NgClass,
    MatProgressSpinner,
    MatOption,
    MatSelect,
    FormsModule,
    MatLabel,
    MatFormField
  ],
  templateUrl: './club-page.component.html',
  standalone: true,
  styleUrl: './club-page.component.css'
})
export class ClubPageComponent implements OnInit {
  userClubs: Club[] = []
  publicClubs: Club[] = []

  showCreateClub = false
  showJoinClub = false
  selectedTabIndex = 0
  isLoading = true

  userId: number
  invites: ClubInvite[] = []

  topClubs: ClubGameStats[] = []
  topClubsByGame: ClubGameStats[] = []
  games: Game[] = []
  selectedGameId: number | null = null
  isLoadingTopClubs = false
  isLoadingGames = false
  isLoadingTopClubsByGame = false

  constructor(
    private router: Router,
    private userService: UserService,
    private authService: AuthService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private clubInviteService: ClubInviteService,
    private statsService: StatsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
  }

  ngOnInit() {
    this.loadUserClubs()
    this.loadPublicClubs()
    this.loadInvites()
    this.loadTopClubs()
    this.loadGames()

    const savedTabIndex = localStorage.getItem("clubTabIndex")
    if (savedTabIndex) {
      this.selectedTabIndex = Number.parseInt(savedTabIndex, 10)
    }
  }

  onTabChange(event: any): void {
    if (event && event.index !== undefined) {
      this.selectedTabIndex = event.index
      localStorage.setItem("clubTabIndex", event.index.toString())

      if (event.index === 0) {
        this.loadUserClubs()
      } else if (event.index === 1) {
        this.loadPublicClubs()
      } else if (event.index === 2) {
        this.loadInvites()
      } else if (event.index === 3) {
        this.loadTopClubs()
        this.loadGames()
      }
    }
  }

  loadUserClubs() {
    this.isLoading = true
    this.clubService.getClubsByUser(this.userId).subscribe({
      next: (clubs) => {
        this.userClubs = clubs
        this.isLoading = false
        if (IS_DEV) console.log("User clubs:", clubs)
      },
      error: (error) => {
        console.error("Error loading user clubs:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_LOAD_USER_CLUBS"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  loadPublicClubs() {
    this.isLoading = true
    this.clubService.getJoinableClubs(this.userId).subscribe({
      next: (clubs) => {
        this.publicClubs = clubs
        this.isLoading = false
        if (IS_DEV) console.log("Public clubs:", clubs)
      },
      error: (error) => {
        console.error("Error loading public clubs:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_LOAD_PUBLIC_CLUBS"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  toggleCreateClub() {
    this.showCreateClub = !this.showCreateClub
    if (this.showCreateClub) {
      this.showJoinClub = false
    }
  }

  toggleJoinClub() {
    this.showJoinClub = !this.showJoinClub
    if (this.showJoinClub) {
      this.showCreateClub = false
    }
  }

  goToClub(clubId: number) {
    this.router.navigate(["/club", clubId])
  }

  loadInvites() {
    this.isLoading = true
    this.clubInviteService.getUserInvites(this.userId).subscribe({
      next: (data) => {
        this.invites = data
        this.isLoading = false
        if (IS_DEV) console.log("Club invites:", data)
      },
      error: (error) => {
        console.error("Error loading invites:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_LOAD_INVITES"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  acceptInvite(id: number) {
    this.isLoading = true
    this.clubInviteService.acceptInvite(id).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant("CLUBS.INVITE_ACCEPTED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        window.location.reload()
      },
      error: (error) => {
        console.error("Error accepting invite:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_ACCEPT_INVITE"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  declineInvite(id: number) {
    this.isLoading = true
    this.clubInviteService.declineInvite(id).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant("CLUBS.INVITE_DECLINED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.loadInvites()
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error declining invite:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_DECLINE_INVITE"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  joinClub(clubId: number) {
    this.isLoading = true
    this.clubMemberService.addMember(clubId, this.userId).subscribe({
      next: () => {
        this.snackBar.open(this.translate.instant("CLUBS.JOIN_SUCCESS"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.loadUserClubs()
        this.loadPublicClubs()
        this.isLoading = false
      },
      error: (error) => {
        console.error("Error joining club:", error)
        this.snackBar.open(this.translate.instant("CLUBS.JOIN_FAILED"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.isLoading = false
      },
    })
  }

  loadTopClubs() {
    this.isLoadingTopClubs = true
    this.statsService.getClubLeaderboard(10).subscribe({
      next: (data) => {
        this.topClubs = data
        this.isLoadingTopClubs = false
        if (IS_DEV) console.log("Top clubs:", data)
      },
      error: (error) => {
        console.error("Error loading top clubs:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_LOAD_LEADERBOARD"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoadingTopClubs = false
      },
    })
  }

  loadGames() {
    this.isLoadingGames = true
    this.statsService.getAllGames().subscribe({
      next: (data) => {
        this.games = data.filter((game) => game.active)
        if (this.games.length > 0) {
          this.selectedGameId = this.games[0].id
          this.loadTopClubsByGame()
        }
        this.isLoadingGames = false
        if (IS_DEV) console.log("Games:", data)
      },
      error: (error) => {
        console.error("Error loading games:", error)
        this.snackBar.open(this.translate.instant("CLUBS.FAILED_LOAD_GAMES"), this.translate.instant("COMMON.CLOSE"), {
          duration: 3000,
        })
        this.isLoadingGames = false
      },
    })
  }

  loadTopClubsByGame() {
    if (!this.selectedGameId) return

    this.isLoadingTopClubsByGame = true
    this.statsService.getClubLeaderboardByGame(this.selectedGameId, 10).subscribe({
      next: (data) => {
        this.topClubsByGame = data
        this.isLoadingTopClubsByGame = false
        if (IS_DEV) console.log("Top clubs by game:", data)
      },
      error: (error) => {
        console.error("Error loading top clubs by game:", error)
        this.snackBar.open(
          this.translate.instant("CLUBS.FAILED_LOAD_LEADERBOARD"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoadingTopClubsByGame = false
        this.topClubsByGame = []
      },
    })
  }

  onGameChange() {
    this.loadTopClubsByGame()
  }

  refreshData() {
    this.loadUserClubs()
    this.loadPublicClubs()
    this.loadInvites()
    this.loadTopClubs()
    this.loadGames()
  }
}
