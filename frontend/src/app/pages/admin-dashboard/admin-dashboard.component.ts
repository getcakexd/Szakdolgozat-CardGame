import { Component, OnInit, OnDestroy } from "@angular/core"
import { MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { User } from "../../models/user.model"
import { Game } from "../../models/game.model"
import { UserService } from "../../services/user/user.service"
import { GameService } from "../../services/game/game.service"
import { ConfirmDialogComponent } from "../../components/confirm-dialog/confirm-dialog.component"
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
import { NgIf, NgClass, NgFor } from "@angular/common"
import { MatCard } from "@angular/material/card"
import { AddGameDialogComponent } from "../../components/add-game-dialog/add-game-dialog.component"
import { AddUserDialogComponent } from "../../components/add-user-dialog/add-user-dialog.component"
import { AuthService } from "../../services/auth/auth.service"
import { MatTooltip } from "@angular/material/tooltip"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { GameCardComponent } from "../../components/game-card/game-card.component"
import { MatDivider } from "@angular/material/divider"
import { EditGameDialogComponent } from "../../components/edit-game-dialog/edit-game-dialog.component"

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
    NgFor,
    MatTabGroup,
    MatCard,
    MatTooltip,
    TranslateModule,
    GameCardComponent,
    MatDivider,
  ],
  standalone: true,
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  users: User[] = []
  games: Game[] = []
  userDisplayedColumns: string[] = ["id", "username", "email", "role", "actions"]
  isMobile = window.innerWidth < 768

  isRoot = false
  isAdmin = false

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private gameService: GameService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadGames()
    this.isRoot = this.authService.isRoot()
    this.isAdmin = this.authService.isAdmin()
    this.updateDisplayColumns()
    window.addEventListener("resize", this.onResize.bind(this))
  }

  ngOnDestroy(): void {
    window.removeEventListener("resize", this.onResize.bind(this))
  }

  onResize(event: any): void {
    this.isMobile = window.innerWidth < 768
    this.updateDisplayColumns()
  }

  updateDisplayColumns(): void {
    if (this.isMobile) {
      this.userDisplayedColumns = ["username", "role", "actions"]
    } else {
      this.userDisplayedColumns = ["id", "username", "email", "role", "actions"]
    }
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (users) => (this.users = users),
      () => this.showError(this.translate.instant("ADMIN.FAILED_LOAD_USERS")),
    )
  }

  loadGames(): void {
    this.gameService.getAllGames().subscribe(
      (games) => (this.games = games),
      () => this.showError(this.translate.instant("ADMIN.FAILED_LOAD_GAMES")),
    )
  }

  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: this.isMobile ? "90%" : "20%",
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.adminCreateUser(result).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.USER_CREATED"))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_CREATE_USER")),
        )
      }
    })
  }

  openAddGameDialog(): void {
    const dialogRef = this.dialog.open(AddGameDialogComponent, {
      width: this.isMobile ? "95%" : "90%",
      maxWidth: "1200px",
      panelClass: "wide-dialog",
      autoFocus: false,
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.createGame(result).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.GAME_CREATED"))
            this.loadGames()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_CREATE_GAME")),
        )
      }
    })
  }

  deleteUser(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_DELETE_USER", { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.adminDeleteUser(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.USER_DELETED"))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_DELETE_USER")),
        )
      }
    })
  }

  deleteGame(game: Game): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_DELETE_GAME", { name: game.name }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.deleteGame(game.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.GAME_DELETED"))
            this.loadGames()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_DELETE_GAME")),
        )
      }
    })
  }

  toggleGameActive(event: { game: Game; active: boolean }): void {
    const game = { ...event.game, active: event.active }

    const updateData = {
      id: game.id,
      name: game.name,
      active: game.active,
      minPlayers: game.minPlayers,
      maxPlayers: game.maxPlayers,
      descriptions: game.descriptions,
      rules: game.rules,
      factorySign: game.factorySign,
      factoryId: game.factoryId,
    }

    this.gameService.updateGame(game.id, updateData).subscribe(
      () => {
        this.showSuccess(this.translate.instant("ADMIN.GAME_UPDATED"))
        this.loadGames()
      },
      () => this.showError(this.translate.instant("ADMIN.FAILED_UPDATE_GAME")),
    )
  }

  promoteToAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_PROMOTION_AGENT", { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAgent(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.PROMOTED_AGENT", { username: user.username }))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_PROMOTE_USER")),
        )
      }
    })
  }

  demoteFromAgent(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_DEMOTION_AGENT", { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAgent(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.DEMOTED_AGENT", { username: user.username }))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_DEMOTE_USER")),
        )
      }
    })
  }

  promoteToAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_PROMOTION_ADMIN", { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.promoteToAdmin(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.PROMOTED_ADMIN", { username: user.username }))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_PROMOTE_USER")),
        )
      }
    })
  }

  demoteFromAdmin(user: User): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: this.isMobile ? "90%" : "300px",
      data: {
        title: this.translate.instant("COMMON.CONFIRM"),
        message: this.translate.instant("ADMIN.CONFIRM_DEMOTION_ADMIN", { username: user.username }),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.demoteFromAdmin(user.id).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.DEMOTED_ADMIN", { username: user.username }))
            this.loadUsers()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_DEMOTE_USER")),
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
    this.snackBar.open(message, this.translate.instant("COMMON.CLOSE"), { duration: 3000 })
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant("COMMON.CLOSE"), {
      duration: 5000,
      panelClass: "error-snackbar",
    })
  }

  editGame(game: Game): void {
    const dialogRef = this.dialog.open(EditGameDialogComponent, {
      width: this.isMobile ? "95%" : "90%",
      maxWidth: "1200px",
      panelClass: "wide-dialog",
      autoFocus: false,
      data: { game },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.gameService.updateGame(game.id, result).subscribe(
          () => {
            this.showSuccess(this.translate.instant("ADMIN.GAME_UPDATED"))
            this.loadGames()
          },
          () => this.showError(this.translate.instant("ADMIN.FAILED_UPDATE_GAME")),
        )
      }
    })
  }
}
