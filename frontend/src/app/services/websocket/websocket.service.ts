import { Injectable } from "@angular/core"
import {Client, IMessage, Stomp} from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { BehaviorSubject } from "rxjs"
import {BACKEND_API_URL, IS_DEV} from '../../../environments/api-config';

@Injectable({
  providedIn: "root",
})
export class WebSocketService {
  private stompClient: any
  private connected = new BehaviorSubject<boolean>(false)
  connected$ = this.connected.asObservable()
  private apiUrl = BACKEND_API_URL + "/ws"
  private connectionAttempts = 0
  private maxConnectionAttempts = 5
  private subscriptions: { [destination: string]: any } = {}

  constructor() {}

  connect(): void {
    if (this.stompClient && this.stompClient.connected) {
      this.connected.next(true)
      return
    }

    try {
      if (IS_DEV) console.log("Initializing WebSocket connection to:", this.apiUrl)

      const socket = new SockJS(this.apiUrl, null, {
        transports: ["xhr-polling", "xhr-streaming"],
        timeout: 1000,
      })

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
        (frame: any) => {
          if (IS_DEV) console.log("Connected to WebSocket:", frame)
          this.connected.next(true)
          this.connectionAttempts = 0

          Object.entries(this.subscriptions).forEach(([destination, callback]) => {
            this.subscribeInternal(destination, callback)
          })
        },
        (error: any) => {
          if (IS_DEV) console.error("WebSocket connection error:", error)
          this.handleConnectionError("WebSocket connection error")
        },
      )
    } catch (error) {
      if (IS_DEV) console.error("Error initializing WebSocket:", error)
      this.handleConnectionError("Error initializing WebSocket")
    }
  }

  private handleConnectionError(message: string): void {
    this.connected.next(false)
    this.connectionAttempts++

    if (this.connectionAttempts < this.maxConnectionAttempts) {
      if (IS_DEV)
        console.log(
          `Connection attempt ${this.connectionAttempts}/${this.maxConnectionAttempts} failed. Retrying in 5 seconds...`,
        )
      setTimeout(() => this.connect(), 5000)
    } else {
      if (IS_DEV) console.error(`Failed to connect after ${this.maxConnectionAttempts} attempts. Giving up.`)
    }
  }

  subscribe(destination: string, callback: (message: any) => void): void {
    if (IS_DEV) console.log(`Subscribing to ${destination}`)
    this.subscriptions[destination] = callback

    if (this.isConnected()) {
      this.subscribeInternal(destination, callback)
    } else {
      if (IS_DEV) console.log(`Not connected yet. Will subscribe to ${destination} when connected.`)
      this.connect()
    }
  }

  private subscribeInternal(destination: string, callback: (message: any) => void): void {
    if (IS_DEV) console.log(`Actually subscribing to ${destination}`)
    return this.stompClient.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body)
        callback(body)
      } catch (error) {
        if (IS_DEV) console.error(`Error processing message from ${destination}:`, error)
      }
    })
  }

  unsubscribe(destination: string): void {
    if (IS_DEV) console.log(`Unsubscribing from ${destination}`)
    delete this.subscriptions[destination]
  }

  send(destination: string, body: any): void {
    if (this.isConnected()) {
      if (IS_DEV) console.log(`Sending message to ${destination}:`, body)
      this.stompClient.send(destination, {}, JSON.stringify(body))
    } else {
      if (IS_DEV) console.error(`Cannot send message to ${destination}, not connected to WebSocket`)
    }
  }

  isConnected(): boolean {
    return this.stompClient !== null && this.stompClient.connected
  }

  disconnect(): void {
    if (this.stompClient) {
      if (IS_DEV) console.log("Disconnecting WebSocket client")
      this.stompClient.disconnect()
      this.stompClient = null
      this.connected.next(false)
    }
  }

  reconnect(): void {
    if (IS_DEV) console.log("Manually reconnecting WebSocket")
    this.disconnect()
    this.connectionAttempts = 0
    this.connect()
  }
}
