import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { BACKEND_API_URL, IS_DEV } from "../../../environments/api-config"
import { BehaviorSubject, Observable, of } from "rxjs"
import { Stomp } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import {LobbyMessage} from '../../models/lobby-message.mode';

@Injectable({
  providedIn: "root",
})
export class LobbyChatService {
  private apiUrl = BACKEND_API_URL + "/lobbychat"
  private stompClient: any
  private connected = new BehaviorSubject<boolean>(false)

  private messageStore: { [key: string]: BehaviorSubject<LobbyMessage[]> } = {}

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

  subscribeToLobby(lobbyId: number): Observable<LobbyMessage[]> {
    const lobbyKey = `lobby_${lobbyId}`

    if (!this.messageStore[lobbyKey]) {
      this.messageStore[lobbyKey] = new BehaviorSubject<LobbyMessage[]>([])

      this.connect().subscribe((connected) => {
        if (connected) {
          this.stompClient.subscribe("/topic/lobby/" + lobbyId, (message: any) => {
            const data = JSON.parse(message.body)

            if (Array.isArray(data)) {
              this.messageStore[lobbyKey].next(data)
            } else {
              this.handleNewMessage(lobbyId, data)
            }
          })

          this.requestMessages(lobbyId)
        }
      })
    }

    return this.messageStore[lobbyKey].asObservable()
  }

  getMessages(lobbyId: number): Observable<LobbyMessage[]> {
    return this.subscribeToLobby(lobbyId)
  }

  sendMessage(lobbyId: number, senderId: number, content: string): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/lobby.sendMessage",
        {},
        JSON.stringify({
          lobbyId: lobbyId,
          senderId: senderId,
          content: content,
        }),
      )
      return of(null)
    } else {
      return this.http.post<LobbyMessage>(`${this.apiUrl}/send`, null, {
        params: {
          lobbyId: lobbyId.toString(),
          senderId: senderId.toString(),
          content: content,
        },
      })
    }
  }

  unsendMessage(messageId: number): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/lobby.unsendMessage",
        {},
        JSON.stringify({
          messageId: messageId,
        }),
      )
      return of(null)
    } else {
      return this.http.put<any[]>(`${this.apiUrl}/unsend`, null, {
        params: {
          messageId: messageId.toString(),
        },
      })
    }
  }

  removeMessage(messageId: number): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/lobby.removeMessage",
        {},
        JSON.stringify({
          messageId: messageId,
        }),
      )
      return of(null)
    } else {
      return this.http.put<any[]>(`${this.apiUrl}/remove`, null, {
        params: {
          messageId: messageId.toString(),
        },
      })
    }
  }

  private requestMessages(lobbyId: number): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/lobby.getMessages",
        {},
        JSON.stringify({
          lobbyId: lobbyId,
        }),
      )
    } else {
      this.http.get<LobbyMessage[]>(`${this.apiUrl}/lobby/${lobbyId}`).subscribe({
        next: (messages) => {
          const lobbyKey = `lobby_${lobbyId}`
          if (this.messageStore[lobbyKey]) {
            this.messageStore[lobbyKey].next(messages)
          }
        },
        error: (error) => {
          console.error("Error fetching lobby messages via HTTP:", error)
        },
      })
    }
  }

  private handleNewMessage(lobbyId: number, message: LobbyMessage) {
    const lobbyKey = `lobby_${lobbyId}`

    if (this.messageStore[lobbyKey]) {
      const currentMessages = this.messageStore[lobbyKey].value

      const messageExists = currentMessages.some((m) => m.id === message.id)

      if (!messageExists) {
        this.messageStore[lobbyKey].next([...currentMessages, message])
      } else {
        const updatedMessages = currentMessages.map((m) => (m.id === message.id ? message : m))
        this.messageStore[lobbyKey].next(updatedMessages)
      }
    }
  }

  isConnected(): Observable<boolean> {
    return this.connected.asObservable()
  }
}
