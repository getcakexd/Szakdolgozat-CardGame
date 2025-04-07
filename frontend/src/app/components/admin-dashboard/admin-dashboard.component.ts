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
import {AuthService} from '../../services/auth/auth.service';
import {MatTooltip} from '@angular/material/tooltip';

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
      (error) => this.showError("Failed to load users"),
    )
  }

  loadGames(): void {
    this.gameService.getAllGames().subscribe(
      (games) => (this.games = games),
      (error) => this.showError("Failed to load games"),
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
            this.showSuccess("User created successfully")
            this.loadUsers()
          },
          (error) => this.showError("Failed to create user"),
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
            this.showSuccess("Game created successfully")
            this.loadGames()
          },
          (error) => this.showError("Failed to create game"),
        )
      }
    })
  }

  deleteUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: { title: "Confirm Delete", message: `Are you sure you want to delete user ${user.username}?` },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.adminDeleteUser(user.id).subscribe(
          () => {
            this.showSuccess("User deleted successfully")
            this.loadUsers()
          },
          (error) => this.showError("Failed to delete user"),
        )
      }
    })
  }

  deleteGame(game: Game): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: { title: "Confirm Delete", message: `Are you sure you want to delete game ${game.name}?` },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.deleteGame(game.id).subscribe(
          () => {
            this.showSuccess("Game deleted successfully")
            this.loadGames()
          },
          (error) => this.showError("Failed to delete game"),
        )
      }
    })
  }

  promoteToAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: "Confirm Promotion",
        message: `Are you sure you want to promote ${user.username} to Agent?`,
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAgent(user.id).subscribe(
          () => {
            this.showSuccess(`${user.username} has been promoted to Agent`)
            this.loadUsers()
          },
          (error) => this.showError("Failed to promote user"),
        )
      }
    })
  }

  demoteFromAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: "Confirm Demotion",
        message: `Are you sure you want to demote ${user.username} from Agent?`,
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAgent(user.id).subscribe(
          () => {
            this.showSuccess(`${user.username} has been demoted from Agent`)
            this.loadUsers()
          },
          (error) => this.showError("Failed to demote user"),
        )
      }
    })
  }

  promoteToAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: "Confirm Promotion",
        message: `Are you sure you want to promote ${user.username} to Admin?`,
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAdmin(user.id).subscribe(
          () => {
            this.showSuccess(`${user.username} has been promoted to Admin`)
            this.loadUsers()
          },
          (error) => this.showError("Failed to promote user"),
        )
      }
    })
  }

  demoteFromAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "300px",
      data: {
        title: "Confirm Demotion",
        message: `Are you sure you want to demote ${user.username} from Admin?`,
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAdmin(user.id).subscribe(
          () => {
            this.showSuccess(`${user.username} has been demoted from Admin`)
            this.loadUsers()
          },
          (error) => this.showError("Failed to demote user"),
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
    this.snackBar.open(message, "Close", { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, "Close", { duration: 5000, panelClass: "error-snackbar" })
  }
}

