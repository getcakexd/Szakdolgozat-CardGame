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
import {ChatService} from '../../services/chat/chat.service';
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

@Component({
  selector: "app-chat",
  templateUrl: "./chat.component.html",
  styleUrls: ["./chat.component.css"],
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
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild("messageContainer") private messageContainer: ElementRef = {} as ElementRef

  @Input() receiverId: string | number = 0
  @Input() receiverName = ""

  senderId: number = 0
  messages: any[] = []
  newMessage = ""
  isLoading = true

  private subscriptions: Subscription[] = []
  private shouldScrollToBottom = true

  constructor(
    private route: ActivatedRoute,
    protected chatService: ChatService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.currentUser?.id || 0

    if (!this.receiverId) {
      this.subscriptions.push(
        this.route.params.subscribe((params) => {
          this.receiverId = +params["id"]
          if (params["name"]) {
            this.receiverName = params["name"]
          }
        }),
      )
    }

    if (typeof this.receiverId === "string") {
      this.receiverId = Number.parseInt(this.receiverId, 10)
    }

    this.chatService.connect(this.senderId).subscribe((connected) => {
      if (connected) {
        this.loadMessages()
      }
    })
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom()
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
  }

  loadMessages(): void {
    if (!this.receiverId) return

    this.isLoading = true

    this.subscriptions.push(
      this.chatService.getMessages(this.senderId, this.receiverId as number).subscribe({
        next: (data) => {
          this.messages = data.map((msg) => ({
            ...msg,
            unsent: msg.status === "unsent",
          }))
          this.isLoading = false
          this.shouldScrollToBottom = true
        },
        error: (error) => {
          console.error("Error loading messages:", error)
          this.isLoading = false
        },
      }),
    )
  }

  sendMessage(): void {
    if (this.newMessage.trim() && this.receiverId) {
      this.chatService.sendMessage(this.senderId, this.receiverId as number, this.newMessage.trim())
      this.newMessage = ""
      this.shouldScrollToBottom = true
    }
  }

  unsendMessage(messageId: number): void {
    this.chatService.unsendMessage(messageId)
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      this.sendMessage()
    }
  }

  private scrollToBottom(): void {
    try {
      this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight
      this.shouldScrollToBottom = false
    } catch (err) {}
  }
}
