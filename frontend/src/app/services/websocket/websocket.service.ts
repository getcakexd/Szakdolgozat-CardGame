import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { BACKEND_API_URL } from '../../../environments/api-config';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private state = new BehaviorSubject<boolean>(false);
  public connected$ = this.state.asObservable();
  private pendingSubscriptions: Array<{topic: string, callback: (data: any) => void}> = [];
  private sockJsLoaded = false;

  constructor() {
    // Load SockJS script dynamically to avoid "global is not defined" error
    this.loadSockJsScript().then(() => {
      this.initializeClient();
      this.connect();
    });
  }

  private loadSockJsScript(): Promise<void> {
    return new Promise((resolve, reject) => {
      // Check if SockJS is already loaded
      if ((window as any).SockJS) {
        this.sockJsLoaded = true;
        resolve();
        return;
      }

      // Add global polyfill
      (window as any).global = window;

      // Load SockJS script
      const script = document.createElement('script');
      script.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
      script.async = true;
      script.onload = () => {
        this.sockJsLoaded = true;
        resolve();
      };
      script.onerror = (error) => {
        console.error('Failed to load SockJS script', error);
        reject(error);
      };
      document.head.appendChild(script);
    });
  }

  private initializeClient(): void {
    if (!this.sockJsLoaded) {
      console.error('SockJS not loaded yet');
      return;
    }

    // Create Basic Auth header
    const username = 'root';
    const password = 'example';
    const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

    // Get the SockJS constructor from the window object
    const SockJS = (window as any).SockJS;

    this.client = new Client({
      webSocketFactory: () => {
        // Use SockJS with the correct endpoint
        const sockjsInstance = new SockJS(`${BACKEND_API_URL}/ws`);
        console.log('Created SockJS instance:', sockjsInstance);
        return sockjsInstance;
      },
      connectHeaders: {
        Authorization: authHeader
      },
      debug: (msg: string) => {
        console.log('STOMP debug:', msg);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = (frame: any) => {
      console.log('Connected to STOMP server', frame);
      this.state.next(true);

      // Process any pending subscriptions
      this.processPendingSubscriptions();
    };

    this.client.onStompError = (frame: any) => {
      console.error('STOMP error', frame);
      this.state.next(false);
    };

    this.client.onDisconnect = () => {
      console.log('Disconnected from STOMP server');
      this.state.next(false);
    };

    this.client.onWebSocketError = (event: Event) => {
      console.error('WebSocket error:', event);
    };

    this.client.onWebSocketClose = (event: CloseEvent) => {
      console.log('WebSocket closed:', event);
    };
  }

  public connect(): void {
    if (!this.client) {
      console.error('STOMP client not initialized yet');
      return;
    }

    try {
      // Activate the connection
      this.client.activate();
    } catch (error) {
      console.error('Error activating STOMP client:', error);
    }
  }

  private processPendingSubscriptions(): void {
    while (this.pendingSubscriptions.length > 0) {
      const subscription = this.pendingSubscriptions.shift();
      if (subscription) {
        this.subscribeNow(subscription.topic, subscription.callback);
      }
    }
  }

  private subscribeNow(topic: string, callback: (data: any) => void): void {
    if (!this.client) {
      console.error('STOMP client not initialized');
      return;
    }

    try {
      // Create Basic Auth header
      const username = 'root';
      const password = 'example';
      const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

      this.client.subscribe(topic, (message: IMessage) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (e) {
          console.error('Error parsing message body', e);
          callback(message.body);
        }
      }, {
        // Add auth headers to subscription
        Authorization: authHeader
      });
      console.log(`Successfully subscribed to ${topic}`);
    } catch (error) {
      console.error(`Error subscribing to ${topic}`, error);
    }
  }

  public subscribe(topic: string, callback: (data: any) => void): void {
    if (!this.client || !this.client.connected) {
      console.log(`Connection not ready, queuing subscription to ${topic}`);
      this.pendingSubscriptions.push({ topic, callback });
      return;
    }

    this.subscribeNow(topic, callback);
  }

  public send(destination: string, body: any): void {
    if (!this.client || !this.client.connected) {
      console.error('Cannot send message: STOMP client not connected');
      return;
    }

    try {
      // Create Basic Auth header
      const username = 'root';
      const password = 'example';
      const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

      this.client.publish({
        destination,
        body: JSON.stringify(body),
        headers: {
          Authorization: authHeader
        }
      });
    } catch (error) {
      console.error('Error sending message', error);
    }
  }

  public disconnect(): void {
    if (this.client) {
      this.client.deactivate();
    }
    this.state.next(false);
  }

  public isConnected(): boolean {
    return this.state.value;
  }
}
