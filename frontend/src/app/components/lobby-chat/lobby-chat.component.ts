import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewChecked,
} from "@angular/core"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import { Subscription } from "rxjs"
import { LobbyChatService } from "../../services/lobby-chat/lobby-chat.service"
import { AuthService } from "../../services/auth/auth.service"
import { CommonModule, DatePipe } from "@angular/common"
import { MatCardModule } from "@angular/material/card"
import { MatFormFieldModule } from "@angular/material/form-field"
import { MatInputModule } from "@angular/material/input"
import { MatButtonModule } from "@angular/material/button"
import { MatIconModule } from "@angular/material/icon"
import { MatTooltipModule } from "@angular/material/tooltip"
import { MatMenuModule } from "@angular/material/menu"
import {TranslateModule, TranslateService} from "@ngx-translate/core"
import {LobbyMessage} from '../../models/lobby-message.mode';
import {LobbyService} from '../../services/lobby/lobby.service';
import {MatDivider} from '@angular/material/divider';
import {MatProgressSpinner} from '@angular/material/progress-spinner';

@Component({
  selector: "app-lobby-chat",
  templateUrl: "./lobby-chat.component.html",
  styleUrls: ["./lobby-chat.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    TranslateModule,
    DatePipe,
    FormsModule,
    MatDivider,
    MatProgressSpinner,
  ],
})
export class LobbyChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() lobbyName?: string
  @ViewChild("messageContainer") private messageContainer!: ElementRef

  messages: LobbyMessage[] = []
  newMessage = ""
  senderId = 0
  hasPermission = false
  isLoading = true
  lobbyId: number = 0

  private shouldScrollToBottom = true
  private subscriptions: Subscription[] = []

  constructor(
    private authService: AuthService,
    protected lobbyChatService: LobbyChatService,
    private lobbyService: LobbyService,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.currentUser?.id || 0;

    if (this.senderId) {
      this.lobbyService.getLobbyByPlayer(this.senderId).subscribe((lobby) => {
        this.lobbyId = lobby.id;
        this.lobbyName = lobby.code;
        this.hasPermission = lobby.leader.id === this.senderId;

        this.lobbyChatService.connect(this.lobbyId).subscribe((connected) => {
          if (connected) {
            this.loadMessages();
          }
        });
        this.scrollToBottom()
      });
    } else {
      console.warn("No valid sender ID found.");
      this.isLoading = false;
    }
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
  }

  loadMessages() {
    if (!this.lobbyId) return
    this.isLoading = true

    const subscription = this.lobbyChatService.getMessages(this.lobbyId).subscribe({
      next: (messages) => {
        this.messages = messages
        this.shouldScrollToBottom = true;
      },
      error: (error) => {
        console.error(this.translate.instant("CHAT.ERROR_LOADING"), error)
        this.isLoading = false
      },
    })
    this.subscriptions.push(subscription)
  }

  sendMessage() {
    if (!this.newMessage.trim()) return

    this.lobbyChatService.sendMessage(this.lobbyId || 0, this.senderId, this.newMessage).subscribe({
      next: () => {
        this.newMessage = ""
        this.shouldScrollToBottom = true;
        this.scrollToBottom()
      },
      error: (error) => {
        console.error(this.translate.instant("CHAT.ERROR_SENDING"), error)
      },
    })
  }

  unsendMessage(messageId: number) {
    this.lobbyChatService.unsendMessage(messageId).subscribe({
      error: (error) => {
        console.error(this.translate.instant("CHAT.ERROR_UNSENDING"), error)
      },
    })
  }

  removeMessage(messageId: number) {
    this.lobbyChatService.removeMessage(messageId).subscribe({
      error: (error) => {
        console.error(this.translate.instant("CHAT.ERROR_REMOVING"), error)
      },
    })
  }

  scrollToBottom(): void {
    try {
      if (this.messageContainer && this.messageContainer.nativeElement) {
        const element = this.messageContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
      this.shouldScrollToBottom = false;
    } catch (err) {
      console.error("Scroll error:", err);
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault()
      this.sendMessage()
    }
  }
}
