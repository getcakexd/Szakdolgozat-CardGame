import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { BACKEND_API_URL, IS_DEV } from "../../../environments/api-config"
import { BehaviorSubject, Observable, of, firstValueFrom } from "rxjs"
import { Stomp } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { ClubMessage } from "../../models/club-message.model"

@Injectable({
  providedIn: "root",
})
export class ClubChatService {
  private apiUrl = BACKEND_API_URL + "/clubchat"
  private stompClient: any
  private connected = new BehaviorSubject<boolean>(false)
  private connecting = false
  private connectionPromise: Promise<boolean> | null = null

  private messageStore: { [key: string]: BehaviorSubject<ClubMessage[]> } = {}

  constructor(private http: HttpClient) {}

  connect(): Observable<boolean> {
    if (this.stompClient && this.stompClient.connected) {
      return this.connected.asObservable()
    }

    if (this.connecting && this.connectionPromise) {
      return this.connected.asObservable()
    }

    this.connecting = true

    this.connectionPromise = new Promise<boolean>((resolve) => {
      const socket = new SockJS(BACKEND_API_URL + "/ws", null, {
        transports: ["xhr-polling", "xhr-streaming"],
        timeout: 5000,
      })

      if (IS_DEV) console.log("Connecting to WebSocket at:", BACKEND_API_URL + "/ws")

      this.stompClient = Stomp.over(socket)

      if (IS_DEV) {
        this.stompClient.debug = (str: string) => {
          if (IS_DEV) console.log("STOMP: " + str)
        }
      } else {
        this.stompClient.debug = () => {}
      }

      this.stompClient.connect(
        {},
        () => {
          if (IS_DEV) console.log("WebSocket connection established successfully")
          this.connected.next(true)
          this.connecting = false
          resolve(true)
        },
        (error: any) => {
          if (IS_DEV) console.error("WebSocket connection error:", error)
          this.connected.next(false)
          this.connecting = false
          resolve(false)
        },
      )
    })

    return this.connected.asObservable()
  }

  disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect()
    }
    this.connected.next(false)
    this.connecting = false
    this.connectionPromise = null
  }

  async subscribeToClub(clubId: number): Promise<Observable<ClubMessage[]>> {
    const clubKey = `club_${clubId}`

    if (!this.messageStore[clubKey]) {
      this.messageStore[clubKey] = new BehaviorSubject<ClubMessage[]>([])

      try {
        await this.ensureConnected()

        if (this.stompClient && this.stompClient.connected) {
          this.stompClient.subscribe("/topic/club/" + clubId, (message: any) => {
            const data = JSON.parse(message.body)

            if (Array.isArray(data)) {
              this.messageStore[clubKey].next(data)
            } else {
              this.handleNewClubMessage(clubId, data)
            }
          })

          this.requestMessages(clubId)
        } else {
          this.loadMessagesViaHttp(clubId)
        }
      } catch (error) {
        if (IS_DEV) console.error("Error subscribing to club:", error)
        this.loadMessagesViaHttp(clubId)
      }
    }

    return this.messageStore[clubKey].asObservable()
  }

  private async ensureConnected(): Promise<boolean> {
    if (this.stompClient && this.stompClient.connected) {
      return true
    }

    if (this.connectionPromise) {
      return this.connectionPromise
    }

    const connected = await firstValueFrom(this.connect())
    return connected
  }

  private loadMessagesViaHttp(clubId: number) {
    const clubKey = `club_${clubId}`
    this.http
      .get<ClubMessage[]>(`${this.apiUrl}/history`, {
        params: {
          clubId: clubId.toString(),
        },
      })
      .subscribe({
        next: (messages) => {
          if (this.messageStore[clubKey]) {
            this.messageStore[clubKey].next(messages)
          }
        },
        error: (error) => {
          if (IS_DEV) console.error("Error fetching club messages via HTTP:", error)
        },
      })
  }

  getMessages(clubId: number): Observable<ClubMessage[]> {
    const clubKey = `club_${clubId}`

    if (!this.messageStore[clubKey]) {
      this.subscribeToClub(clubId)
    }

    return this.messageStore[clubKey] ? this.messageStore[clubKey].asObservable() : of([])
  }

  async sendMessage(clubId: number, senderId: number, content: string): Promise<Observable<any>> {
    try {
      await this.ensureConnected()

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(
          "/app/club.sendMessage",
          {},
          JSON.stringify({
            clubId: clubId,
            senderId: senderId,
            content: content,
          }),
        )
        return of(null)
      }
    } catch (error) {
      if (IS_DEV) console.error("Error sending message via WebSocket:", error)
    }

    return this.http.post<ClubMessage>(`${this.apiUrl}/send`, null, {
      params: {
        clubId: clubId.toString(),
        senderId: senderId.toString(),
        content: content,
      },
    })
  }

  async unsendMessage(messageId: number): Promise<Observable<any>> {
    try {
      await this.ensureConnected()

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(
          "/app/club.unsendMessage",
          {},
          JSON.stringify({
            messageId: messageId,
          }),
        )
        return of(null)
      }
    } catch (error) {
      if (IS_DEV) console.error("Error unsending message via WebSocket:", error)
    }

    return this.http.put<any[]>(`${this.apiUrl}/unsend`, null, {
      params: {
        messageId: messageId.toString(),
      },
    })
  }

  async removeMessage(messageId: number): Promise<Observable<any>> {
    try {
      await this.ensureConnected()

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(
          "/app/club.removeMessage",
          {},
          JSON.stringify({
            messageId: messageId,
          }),
        )
        return of(null)
      }
    } catch (error) {
      if (IS_DEV) console.error("Error removing message via WebSocket:", error)
    }

    return this.http.put<any[]>(`${this.apiUrl}/remove`, null, {
      params: {
        messageId: messageId.toString(),
      },
    })
  }

  private async requestMessages(clubId: number): Promise<void> {
    try {
      await this.ensureConnected()

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.send(
          "/app/club.getMessages",
          {},
          JSON.stringify({
            clubId: clubId,
          }),
        )
        return
      }
    } catch (error) {
      if (IS_DEV) console.error("Error requesting messages via WebSocket:", error)
    }

    this.loadMessagesViaHttp(clubId)
  }

  private handleNewClubMessage(clubId: number, message: ClubMessage) {
    const clubKey = `club_${clubId}`

    if (this.messageStore[clubKey]) {
      const currentMessages = this.messageStore[clubKey].value
      const messageExists = currentMessages.some((m) => m.id === message.id)

      if (!messageExists) {
        this.messageStore[clubKey].next([...currentMessages, message])
      } else {
        const updatedMessages = currentMessages.map((m) => (m.id === message.id ? message : m))
        this.messageStore[clubKey].next(updatedMessages)
      }
    }
  }

  isConnected(): Observable<boolean> {
    return this.connected.asObservable()
  }
}
