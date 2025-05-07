import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { BACKEND_API_URL, IS_DEV } from "../../../environments/api-config"
import { BehaviorSubject, Observable, of } from "rxjs"
import { Stomp } from "@stomp/stompjs"
import SockJS from "sockjs-client"

@Injectable({
  providedIn: "root",
})
export class ClubChatService {
  private apiUrl = BACKEND_API_URL + "/clubchat"
  private stompClient: any
  private connected = new BehaviorSubject<boolean>(false)

  private messageStore: { [key: string]: BehaviorSubject<any[]> } = {}

  constructor(private http: HttpClient) {}

  connect(): Observable<boolean> {
    if (this.stompClient && this.stompClient.connected) {
      return this.connected.asObservable()
    }

    const socket = new SockJS(BACKEND_API_URL + "/ws", null, {
      transports: ["xhr-polling", "xhr-streaming"],
      timeout: 1000,
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
      },
      (error: any) => {
        if (IS_DEV) console.error("WebSocket connection error:", error)
        this.connected.next(false)
      },
    )

    return this.connected.asObservable()
  }

  disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect()
    }
    this.connected.next(false)
  }

  getMessages(clubId: number): Observable<any[]> {
    const clubKey = `club_${clubId}`

    if (!this.messageStore[clubKey]) {
      this.messageStore[clubKey] = new BehaviorSubject<any[]>([])

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.subscribe("/topic/club/" + clubId, (message: any) => {
          const messages = JSON.parse(message.body)
          if (Array.isArray(messages)) {
            this.messageStore[clubKey].next(messages)
          } else {
            this.handleNewClubMessage(clubId, messages)
          }
        })
      }
    }

    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/club.getMessages",
        {},
        JSON.stringify({
          clubId: clubId,
        }),
      )
    } else {
      this.http
        .get<any[]>(`${this.apiUrl}/history`, {
          params: {
            clubId: clubId.toString(),
          },
        })
        .subscribe({
          next: (messages) => {
            this.messageStore[clubKey].next(messages)
          },
          error: (error) => {
            console.error("Error fetching club messages via HTTP:", error)
          },
        })
    }

    return this.messageStore[clubKey].asObservable()
  }

  sendMessage(clubId: number, senderId: number, content: string): Observable<any> {
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
    } else {
      return this.http.post<any>(`${this.apiUrl}/send`, null, {
        params: {
          clubId: clubId.toString(),
          senderId: senderId.toString(),
          content: content,
        },
      })
    }
  }

  unsendMessage(messageId: number): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/club.unsendMessage",
        {},
        JSON.stringify({
          messageId: messageId,
        }),
      )
      return of(null)
    } else {
      return this.http.put<any>(`${this.apiUrl}/unsend`, null, {
        params: {
          messageId: messageId.toString(),
        },
      })
    }
  }

  removeMessage(messageId: number): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/club.removeMessage",
        {},
        JSON.stringify({
          messageId: messageId,
        }),
      )
      return of(null)
    } else {
      return this.http.put<any>(`${this.apiUrl}/remove`, null, {
        params: {
          messageId: messageId.toString(),
        },
      })
    }
  }

  private handleNewClubMessage(clubId: number, message: any) {
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
