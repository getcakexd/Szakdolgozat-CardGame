import { Injectable } from "@angular/core"
import { Client, IMessage } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { BehaviorSubject } from "rxjs"
import {BACKEND_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: "root",
})
export class WebSocketService {
  private client: Client | null = null
  private subscriptions: { [destination: string]: (message: any) => void } = {}
  private connected = new BehaviorSubject<boolean>(false)
  connected$ = this.connected.asObservable()
  private apiUrl = BACKEND_API_URL + "/ws"
  private connectionAttempts = 0
  private maxConnectionAttempts = 5

  constructor() {
    console.log("WebSocket service initialized with URL:", this.apiUrl)
    this.initializeWebSocketConnection()
  }

  private initializeWebSocketConnection(): void {
    try {
      console.log("Initializing WebSocket connection to:", this.apiUrl)

      this.client = new Client({
        webSocketFactory: () => new SockJS(this.apiUrl),
        debug: (str) => {
          console.log("STOMP: " + str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      })

      this.client.onConnect = (frame) => {
        console.log("Connected to WebSocket:", frame)
        this.connected.next(true)
        this.connectionAttempts = 0

        Object.entries(this.subscriptions).forEach(([destination, callback]) => {
          this.subscribeInternal(destination, callback)
        })
      }

      this.client.onDisconnect = () => {
        console.log("Disconnected from WebSocket")
        this.connected.next(false)
      }

      this.client.onStompError = (frame) => {
        console.error("STOMP error", frame)
        this.handleConnectionError("STOMP error: " + frame.headers?.["message"])
      }

      this.client.onWebSocketError = (event) => {
        console.error("WebSocket error:", event)
        this.handleConnectionError("WebSocket error")
      }

      this.client.activate()
    } catch (error) {
      console.error("Error initializing WebSocket:", error)
      this.handleConnectionError("Error initializing WebSocket")
    }
  }

  private handleConnectionError(message: string): void {
    this.connected.next(false)
    this.connectionAttempts++

    if (this.connectionAttempts < this.maxConnectionAttempts) {
      console.log(
        `Connection attempt ${this.connectionAttempts}/${this.maxConnectionAttempts} failed. Retrying in 5 seconds...`,
      )
      setTimeout(() => this.initializeWebSocketConnection(), 5000)
    } else {
      console.error(`Failed to connect after ${this.maxConnectionAttempts} attempts. Giving up.`)
    }
  }

  subscribe(destination: string, callback: (message: any) => void): void {
    console.log(`Subscribing to ${destination}`)
    this.subscriptions[destination] = callback

    if (this.isConnected()) {
      this.subscribeInternal(destination, callback)
    } else {
      console.log(`Not connected yet. Will subscribe to ${destination} when connected.`)
    }
  }

  private subscribeInternal(destination: string, callback: (message: any) => void): void {
    console.log(`Actually subscribing to ${destination}`)
    this.client?.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body)
        callback(body)
      } catch (error) {
        console.error(`Error processing message from ${destination}:`, error)
      }
    })
  }

  unsubscribe(destination: string): void {
    console.log(`Unsubscribing from ${destination}`)
    delete this.subscriptions[destination]
  }

  send(destination: string, body: any): void {
    if (this.isConnected()) {
      console.log(`Sending message to ${destination}:`, body)
      this.client!.publish({
        destination,
        body: JSON.stringify(body),
      })
    } else {
      console.error(`Cannot send message to ${destination}, not connected to WebSocket`)
    }
  }

  isConnected(): boolean {
    return this.client !== null && this.client.connected
  }

  disconnect(): void {
    if (this.client) {
      console.log("Disconnecting WebSocket client")
      this.client.deactivate()
      this.client = null
      this.connected.next(false)
    }
  }

  reconnect(): void {
    console.log("Manually reconnecting WebSocket")
    this.disconnect()
    this.connectionAttempts = 0
    this.initializeWebSocketConnection()
  }
}
