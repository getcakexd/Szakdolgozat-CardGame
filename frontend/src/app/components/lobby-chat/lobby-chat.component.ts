import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
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
import {IS_DEV} from '../../../environments/api-config';

@Component({
  selector: "app-lobby-chat",
  templateUrl: "./lobby-chat.component.html",
  styleUrls: ["./lobby-chat.component.css"],
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
export class LobbyChatComponent implements OnInit, OnDestroy {
  @Input() lobbyId!: number
  @Input() lobbyName?: string
  @ViewChild("messageContainer") private messageContainer!: ElementRef

  messages: LobbyMessage[] = []
  newMessage = ""
  senderId = 0
  hasPermission = false
  isLoading = true

  private subscriptions: Subscription[] = []

  constructor(
    private authService: AuthService,
    protected lobbyChatService: LobbyChatService,
    private lobbyService: LobbyService,
    private translate: TranslateService,
  ) {}

  ngOnInit(): void {
    this.senderId = this.authService.currentUser?.id || 0
    if (this.lobbyId) {
      this.lobbyService.getLobby(this.lobbyId).subscribe((lobby) => {
        this.lobbyName = lobby.code
        this.hasPermission = lobby.leader.id == this.senderId;
      })
    }

    this.loadMessages()

    this.lobbyChatService.connect().subscribe()
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
        this.scrollToBottom()
        this.isLoading = false
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

    this.lobbyChatService.sendMessage(this.lobbyId, this.senderId, this.newMessage).subscribe({
      next: () => {
        this.newMessage = ""
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
