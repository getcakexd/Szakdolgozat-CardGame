import { Component, ElementRef, Input, OnInit, OnDestroy, ViewChild } from "@angular/core"
import { ClubChatService } from "../../services/club-chat/club-chat.service"
import { ClubMessage } from "../../models/club-message.model"
import { FormsModule } from "@angular/forms"
import { ClubService } from "../../services/club/club.service"
import { Club } from "../../models/club.model"
import { AsyncPipe, DatePipe, NgClass, NgForOf, NgIf} from "@angular/common"
import { ClubMemberService } from "../../services/club-member/club-member.service"
import { MatCardModule } from "@angular/material/card"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatInputModule } from "@angular/material/input"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatDividerModule } from "@angular/material/divider"
import { MatTooltipModule } from "@angular/material/tooltip"
import { MatBadgeModule } from "@angular/material/badge"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import { AuthService } from "../../services/auth/auth.service"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { Subscription } from "rxjs"

@Component({
  selector: "app-club-chat",
  imports: [
    FormsModule,
    NgForOf,
    NgClass,
    DatePipe,
    NgIf,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDividerModule,
    MatTooltipModule,
    MatBadgeModule,
    MatProgressSpinnerModule,
    TranslateModule,
    AsyncPipe,
  ],
  templateUrl: "./club-chat.component.html",
  standalone: true,
  styleUrls: ["./club-chat.component.css"],
})
export class ClubChatComponent implements OnInit, OnDestroy {
  @Input() clubId!: number
  @Input() clubName!: string
  @ViewChild("messageContainer") private messageContainer!: ElementRef

  club: any
  messages: ClubMessage[] = []
  newMessage = ""
  senderId = 0
  hasPermission = false
  isLoading = false

  private subscriptions: Subscription[] = []

  constructor(
    private authService: AuthService,
    protected clubChatService: ClubChatService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.getCurrentUserId() || 0
    this.getClub(this.clubId)
    this.loadMessages()
    this.setHasPermission()

    this.clubChatService.connect().subscribe()
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
  }

  getClub(clubId: number) {
    this.clubService.getClub(clubId).subscribe((club: Club) => {
      this.club = club
    })
  }

  loadMessages() {
    if (!this.clubId) return

    this.isLoading = true

    const subscription = this.clubChatService.getMessages(this.clubId).subscribe({
      next: (messages) => {
        this.messages = messages
        this.scrollToBottom()
        this.isLoading = false
      },
      error: (error) => {
        console.error(this.translate.instant("CLUB_CHAT.ERROR_LOADING"), error)
        this.isLoading = false
      },
    })

    this.subscriptions.push(subscription)
  }

  setHasPermission() {
    this.clubMemberService.getUserRole(this.clubId, this.senderId).subscribe((role) => {
      this.hasPermission = role.role === "admin" || role.role === "moderator"
    })
  }

  sendMessage() {
    if (!this.newMessage.trim()) return

    this.clubChatService.sendMessage(this.clubId, this.senderId, this.newMessage).subscribe({
      next: () => {
        this.newMessage = ""
        this.scrollToBottom()
      },
      error: (error) => {
        console.error(this.translate.instant("CLUB_CHAT.ERROR_SENDING"), error)
      },
    })
  }

  unsendMessage(messageId: number) {
    this.clubChatService.unsendMessage(messageId).subscribe({
      error: (error) => {
        console.error(this.translate.instant("CLUB_CHAT.ERROR_UNSENDING"), error)
      },
    })
  }

  removeMessage(messageId: number) {
    this.clubChatService.removeMessage(messageId).subscribe({
      error: (error) => {
        console.error(this.translate.instant("CLUB_CHAT.ERROR_REMOVING"), error)
      },
    })
  }

  private scrollToBottom(): void {
    try {
      setTimeout(() => {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight
      }, 100)
    } catch (err) {
      console.error("Scroll error:", err)
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      this.sendMessage()
    }
  }
}
