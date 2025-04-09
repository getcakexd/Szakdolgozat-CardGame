import { Component, type OnInit } from "@angular/core"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { User } from "../../models/user.model"
import { Game } from "../../models/game.model"
import { UserService } from "../../services/user/user.service"
import { GameService } from "../../services/game/game.service"
import { ConfirmDialogComponent } from "../confirm-dialog/confirm-dialog.component"
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
} from "@angular/material/table"
import { MatIcon } from "@angular/material/icon"
import { MatTab, MatTabGroup } from "@angular/material/tabs"
import { MatButton, MatIconButton } from "@angular/material/button"
import { NgIf, NgClass } from "@angular/common"
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle } from "@angular/material/card"
import { AddGameDialogComponent } from "../add-game-dialog/add-game-dialog.component"
import { AddUserDialogComponent } from "../add-user-dialog/add-user-dialog.component"
import { AuthService } from '../../services/auth/auth.service';
import { MatTooltip } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: "app-admin-dashboard",
  templateUrl: "./admin-dashboard.component.html",
  styleUrls: ["./admin-dashboard.component.scss"],
  imports: [
    MatTable,
    MatHeaderCellDef,
    MatCellDef,
    MatIcon,
    MatTab,
    MatColumnDef,
    MatButton,
    MatHeaderCell,
    MatCell,
    MatIconButton,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRowDef,
    MatRow,
    NgIf,
    NgClass,
    MatTabGroup,
    MatCardContent,
    MatCardTitle,
    MatCardHeader,
    MatCard,
    MatTooltip,
    TranslateModule
  ],
  standalone: true,
})
export class AdminDashboardComponent implements OnInit {
  users: User[] = []
  games: Game[] = []
  userDisplayedColumns: string[] = ["id", "username", "email", "role", "actions"]
  gameDisplayedColumns: string[] = ["id", "name", "description", "active", "actions"]

  isRoot = false
  isAdmin = false

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private gameService: GameService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadGames()
    this.isRoot = this.authService.isRoot();
    this.isAdmin = this.authService.isAdmin();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => (this.users = users),
      (error) => this.showError(this.translate.instant('ADMIN.FAILED_LOAD_USERS')),
    )
  }

  loadGames(): void {
    this.gameService.getAllGames().subscribe(
      (games) => (this.games = games),
      (error) => this.showError(this.translate.instant('ADMIN.FAILED_LOAD_GAMES')),
    )
  }

  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: "400px",
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.adminCreateUser(result).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.USER_CREATED'))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_CREATE_USER')),
        )
      }
    })
  }

  openAddGameDialog(): void {
    const dialogRef = this.dialog.open(AddGameDialogComponent, {
      width: "400px",
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.createGame(result).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.GAME_CREATED'))
            this.loadGames()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_CREATE_GAME')),
        )
      }
    })
  }

  deleteUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_DELETE_USER', { username: user.username })
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.adminDeleteUser(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.USER_DELETED'))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_DELETE_USER')),
        )
      }
    })
  }

  deleteGame(game: Game): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_DELETE_GAME', { name: game.name })
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.deleteGame(game.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.GAME_DELETED'))
            this.loadGames()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_DELETE_GAME')),
        )
      }
    })
  }

  promoteToAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_PROMOTION_AGENT', { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAgent(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.PROMOTED_AGENT', { username: user.username }))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_PROMOTE_USER')),
        )
      }
    })
  }

  demoteFromAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_DEMOTION_AGENT', { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAgent(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.DEMOTED_AGENT', { username: user.username }))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_DEMOTE_USER')),
        )
      }
    })
  }

  promoteToAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_PROMOTION_ADMIN', { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAdmin(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.PROMOTED_ADMIN', { username: user.username }))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_PROMOTE_USER')),
        )
      }
    })
  }

  demoteFromAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: this.translate.instant('COMMON.CONFIRM'),
        message: this.translate.instant('ADMIN.CONFIRM_DEMOTION_ADMIN', { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAdmin(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant('ADMIN.DEMOTED_ADMIN', { username: user.username }))
            this.loadUsers()
          },
          (error) => this.showError(this.translate.instant('ADMIN.FAILED_DEMOTE_USER')),
        )
      }
    })
  }

  formatRole(role: string): string {
    return this.userService.formatRole(role)
  }

  canPromoteToAgent(user: User): boolean {
    return user.role === "ROLE_USER"
  }

  canDemoteFromAgent(user: User): boolean {
    return user.role === "ROLE_AGENT"
  }

  canPromoteToAdmin(user: User): boolean {
    return this.isRoot && (user.role === "ROLE_AGENT" || user.role === "ROLE_USER")
  }

  canDemoteFromAdmin(user: User): boolean {
    return this.isRoot && user.role === "ROLE_ADMIN"
  }

  getRoleClass(role: string): string {
    const formattedRole = this.formatRole(role).toLowerCase()
    return `role-${formattedRole}`
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant('COMMON.CLOSE'), { duration: 5000, panelClass: "error-snackbar" })
  }
}
