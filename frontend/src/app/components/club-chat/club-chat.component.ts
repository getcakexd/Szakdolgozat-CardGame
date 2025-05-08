import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewChecked,
} from "@angular/core"
import { ActivatedRoute } from "@angular/router"
import { Subscription } from "rxjs"
import {ClubChatService} from '../../services/club-chat/club-chat.service';
import {AuthService} from '../../services/auth/auth.service';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton, MatMiniFabButton} from '@angular/material/button';
import {AsyncPipe, DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {FormsModule} from '@angular/forms';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatDivider} from '@angular/material/divider';
import {MatTooltip} from '@angular/material/tooltip';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {ClubService} from "../../services/club/club.service";
import {ClubMemberService} from "../../services/club-member/club-member.service";
import {IS_DEV} from '../../../environments/api-config';

@Component({
  selector: "app-club-chat",
  templateUrl: "./club-chat.component.html",
  styleUrls: ["./club-chat.component.scss"],
  imports: [
    MatIcon,
    MatMiniFabButton,
    AsyncPipe,
    TranslatePipe,
    FormsModule,
    MatInput,
    MatLabel,
    MatFormField,
    MatDivider,
    MatIconButton,
    MatTooltip,
    DatePipe,
    NgIf,
    NgClass,
    NgForOf,
    MatProgressSpinner,
    MatCardContent,
    MatCardTitle,
    MatCardHeader,
    MatCard
  ],
  standalone: true
})
export class ClubChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild("messageContainer") private messageContainer!: ElementRef;

  @Input() clubId: number = 0
  @Input() clubName = ""

  senderId: number = 0
  messages: any[] = []
  newMessage = ""
  isLoading = true
  hasPermission = false

  private subscriptions: Subscription[] = []
  private shouldScrollToBottom = true

  constructor(
    private route: ActivatedRoute,
    protected clubChatService: ClubChatService,
    private authService: AuthService,
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.currentUser?.id || 0

    if (!this.clubId) {
      this.subscriptions.push(
        this.route.params.subscribe((params) => {
          this.clubId = +params["id"]
          if (params["name"]) {
            this.clubName = params["name"]
          }
        }),
      )
    }

    this.clubChatService.connect(this.clubId).subscribe((connected) => {
      if (connected) {
        this.loadMessages()
      }
    })

    this.scrollToBottom()

    this.setHasPermission()
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom()
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
  }

  setHasPermission() {
    this.clubMemberService.getUserRole(this.clubId, this.senderId).subscribe((role) => {
      this.hasPermission = role.role === "admin" || role.role === "moderator"
    })
  }

  loadMessages(): void {
    if (!this.clubId) return

    this.isLoading = true

    this.subscriptions.push(
      this.clubChatService.getMessages(this.clubId).subscribe({
        next: (data) => {
          this.messages = data
          this.isLoading = false
          this.shouldScrollToBottom = true
        },
        error: (error) => {
          if(IS_DEV) console.error("Error loading messages:", error)
          this.isLoading = false
        },
      }),
    )
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.clubId) {
      this.clubChatService.sendMessage(this.clubId, this.senderId, this.newMessage.trim())
      this.newMessage = ""
      this.shouldScrollToBottom = true
      this.scrollToBottom()
    }
  }

  unsendMessage(messageId: number): void {
    this.clubChatService.unsendMessage(messageId)
  }

  removeMessage(messageId: number): void {
    if (this.hasPermission) {
      this.clubChatService.removeMessage(messageId)
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      this.sendMessage()
    }
  }

  scrollToBottom(): void {
    try {
      if (this.messageContainer && this.messageContainer.nativeElement) {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      }
      this.shouldScrollToBottom = false;
    } catch (err) {
      if(IS_DEV) console.error("Error scrolling to bottom:", err);
    }
  }
}
