import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import {BACKEND_API_URL, IS_DEV} from "../../../environments/api-config"
import { BehaviorSubject, Observable, of } from "rxjs"
import { Stomp } from "@stomp/stompjs"
import SockJS from "sockjs-client"

@Injectable({
  providedIn: "root",
})
export class ChatService {
  private apiUrl = BACKEND_API_URL + "/messages"
  private stompClient: any
  private connected = new BehaviorSubject<boolean>(false)

  private messageStore: { [key: string]: BehaviorSubject<any[]> } = {}
  private unreadCountsSubject = new BehaviorSubject<{ [key: number]: number }>({})

  constructor(private http: HttpClient) {}

  connect(userId: number): Observable<boolean> {
    if (this.stompClient && this.stompClient.connected) {
      return this.connected.asObservable()
    }

    const socket = new SockJS(BACKEND_API_URL + "/ws")

    console.log("Connecting to WebSocket at:", BACKEND_API_URL + "/ws")

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

        this.stompClient.subscribe("/topic/user/" + userId, (message: any) => {
          if (IS_DEV) console.log("Received message on user topic:", message)
          const receivedMessage = JSON.parse(message.body)
          this.handleNewMessage(receivedMessage)
        })

        this.stompClient.subscribe("/topic/unread/" + userId, (message: any) => {
          if (IS_DEV) console.log("Received unread counts:", message)
          const unreadCounts = JSON.parse(message.body)
          this.unreadCountsSubject.next(unreadCounts)
        })

        this.requestUnreadCounts(userId)

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

  getMessages(senderId: number, receiverId: number): Observable<any[]> {
    const conversationKey = `${senderId}_${receiverId}`

    if (!this.messageStore[conversationKey]) {
      this.messageStore[conversationKey] = new BehaviorSubject<any[]>([])

      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.subscribe("/topic/chat/" + senderId + "/" + receiverId, (message: any) => {
          const messages = JSON.parse(message.body)
          this.messageStore[conversationKey].next(messages)
        })
      }
    }

    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/chat.getMessages",
        {},
        JSON.stringify({
          userId: senderId,
          friendId: receiverId,
        }),
      )
    } else {
      this.http
        .get<any[]>(`${this.apiUrl}/list`, {
          params: {
            userId: senderId.toString(),
            friendId: receiverId.toString(),
          },
        })
        .subscribe({
          next: (messages) => {
            this.messageStore[conversationKey].next(messages)
          },
          error: (error) => {
            console.error("Error fetching messages via HTTP:", error)
          },
        })
    }

    return this.messageStore[conversationKey].asObservable()
  }

  sendMessage(senderId: number, receiverId: number, content: string): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/chat.sendMessage",
        {},
        JSON.stringify({
          senderId: senderId,
          receiverId: receiverId,
          content: content,
        }),
      )
      return of(null)
    } else {
      return this.http.post<any>(`${this.apiUrl}/send`, null, {
        params: {
          userId: senderId.toString(),
          friendId: receiverId.toString(),
          content: content,
        },
      })
    }
  }

  unsendMessage(messageId: number): Observable<any> {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/chat.unsendMessage",
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

  getUnreadCounts(): Observable<{ [key: number]: number }> {
    return this.unreadCountsSubject.asObservable()
  }

  getUnreadMessagesPerFriend(userId: number): Observable<{ [key: number]: number }> {
    this.requestUnreadCounts(userId)

    return this.unreadCountsSubject.asObservable()
  }

  private requestUnreadCounts(userId: number): void {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send(
        "/app/chat.getUnreadCounts",
        {},
        JSON.stringify({
          userId: userId,
        }),
      )
    } else {
      this.http
        .get<{ [key: number]: number }>(`${this.apiUrl}/unread`, {
          params: {
            userId: userId.toString(),
          },
        })
        .subscribe({
          next: (counts) => {
            this.unreadCountsSubject.next(counts)
          },
          error: (error) => {
            console.error("Error fetching unread counts via HTTP:", error)
          },
        })
    }
  }

  private handleNewMessage(message: any) {
    const senderId = message.sender.id
    const receiverId = message.receiver.id

    const conversationKeys = [`${senderId}_${receiverId}`, `${receiverId}_${senderId}`]

    conversationKeys.forEach((key) => {
      if (this.messageStore[key]) {
        const currentMessages = this.messageStore[key].value

        const messageExists = currentMessages.some((m) => m.id === message.id)

        if (!messageExists) {
          this.messageStore[key].next([...currentMessages, message])
        } else {
          const updatedMessages = currentMessages.map((m) => (m.id === message.id ? message : m))
          this.messageStore[key].next(updatedMessages)
        }
      }
    })
  }

  isConnected(): Observable<boolean> {
    return this.connected.asObservable()
  }
}
