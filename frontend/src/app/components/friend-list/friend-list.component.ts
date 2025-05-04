import { Component, OnInit, OnDestroy } from "@angular/core"
import { Friend } from "../../models/friend.model"
import { NgForOf, NgIf } from "@angular/common"
import { MatListModule } from "@angular/material/list"
import { MatButtonModule } from "@angular/material/button"
import { MatBadgeModule } from "@angular/material/badge"
import { MatIconModule } from "@angular/material/icon"
import { MatCardModule } from "@angular/material/card"
import { MatExpansionModule } from "@angular/material/expansion"
import { MatDividerModule } from "@angular/material/divider"
import { MatTooltipModule } from "@angular/material/tooltip"
import { MatDialog, MatDialogModule } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { ChatComponent } from "../chat/chat.component"
import { FriendshipService } from "../../services/friendship/friendship.service"
import { ChatService } from "../../services/chat/chat.service"
import { ConfirmDialogComponent } from "../confirm-dialog/confirm-dialog.component"
import { MatProgressSpinner } from "@angular/material/progress-spinner"
import { AuthService } from "../../services/auth/auth.service"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { Subscription } from "rxjs"

@Component({
  selector: "app-friend-list",
  templateUrl: "./friend-list.component.html",
  styleUrls: ["./friend-list.component.scss"],
  imports: [
    NgForOf,
    NgIf,
    MatListModule,
    MatButtonModule,
    MatBadgeModule,
    MatIconModule,
    MatCardModule,
    MatExpansionModule,
    MatDividerModule,
    MatTooltipModule,
    MatDialogModule,
    ChatComponent,
    MatProgressSpinner,
    TranslateModule,
  ],
  standalone: true,
})
export class FriendListComponent implements OnInit, OnDestroy {
  friends: Friend[] = []
  unreadCounts: { [key: number]: number } = {}
  openChats: { [key: number]: boolean } = {}
  userId = 0
  isLoading = false

  private subscriptions: Subscription[] = []

  constructor(
    private authService: AuthService,
    private friendshipService: FriendshipService,
    private chatService: ChatService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
  ) {
    this.userId = this.authService.getCurrentUserId() || 0
  }

  ngOnInit(): void {
    this.loadFriends()

    this.subscriptions.push(
      this.chatService.getUnreadCounts().subscribe((counts) => {
        this.unreadCounts = counts
      }),
    )
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
  }

  loadFriends(): void {
    this.isLoading = true
    this.friendshipService.getFriends(this.userId).subscribe({
      next: (data: Friend[]) => {
        this.friends = data
        this.loadUnreadCounts()
        this.isLoading = false
      },
      error: (error: any) => {
        console.error("Error loading friends:", error)
        this.snackBar.open(
          this.translate.instant("SOCIAL.FAILED_LOAD_FRIENDS"),
          this.translate.instant("COMMON.CLOSE"),
          { duration: 3000 },
        )
        this.isLoading = false
      },
    })
  }

  loadUnreadCounts(): void {
    this.chatService.getUnreadMessagesPerFriend(this.userId).subscribe({
      next: (data: { [key: number]: number }) => {
        this.unreadCounts = data
      },
      error: (error: any) => {
        console.error("Error loading unread counts:", error)
      },
    })
  }

  deleteFriend(friendId: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.translate.instant("SOCIAL.REMOVE_FRIEND"),
        message: this.translate.instant("SOCIAL.CONFIRM_REMOVE_FRIEND"),
      },
    })

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.friendshipService.deleteFriend(this.userId, Number.parseInt(friendId)).subscribe({
          next: () => {
            this.snackBar.open(
              this.translate.instant("SOCIAL.FRIEND_REMOVED"),
              this.translate.instant("COMMON.CLOSE"),
              { duration: 3000 },
            )
            this.loadFriends()
          },
          error: (error: any) => {
            console.error("Error removing friend:", error)
            this.snackBar.open(
              this.translate.instant("SOCIAL.FAILED_REMOVE_FRIEND"),
              this.translate.instant("COMMON.CLOSE"),
              { duration: 3000 },
            )
          },
        })
      }
    })
  }

  toggleChat(friendId: string): void {
    const friendIdNum = Number.parseInt(friendId)
    this.openChats[friendIdNum] = !this.openChats[friendIdNum]

    if (this.openChats[friendIdNum] && this.unreadCounts[friendIdNum] > 0) {
      this.unreadCounts[friendIdNum] = 0
    }
  }

  protected readonly parseInt = Number.parseInt
}
