import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import { BACKEND_API_URL } from '../../../environments/api-config';
import { UserService } from '../user/user.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private state = new BehaviorSubject<boolean>(false);
  public connected$ = this.state.asObservable();
  private pendingSubscriptions: Array<{topic: string, callback: (data: any) => void}> = [];
  private sockJsLoaded = false;

  constructor(private userService: UserService) {
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

    // Get credentials from UserService
    const username = this.userService.getLoggedInUsername();
    const password = this.userService.getLoggedInPassword();

    // Create Basic Auth header
    const authHeader = `Basic ${btoa(`${username}:${password}`)}`;

    // Get the SockJS constructor from the window object
    const SockJS = (window as any).SockJS;

    // Make sure we're using the correct endpoint
    const wsEndpoint = `${BACKEND_API_URL}/ws`;
    console.log('Connecting to WebSocket endpoint:', wsEndpoint);

    this.client = new Client({
      webSocketFactory: () => {
        // Use SockJS with the correct endpoint
        const sockjsInstance = new SockJS(wsEndpoint);
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
      console.error('STOMP client not initialized');
      return;
    }

    if (this.client.active) {
      console.log('Already connected to STOMP server');
      return;
    }

    console.log('Connecting to STOMP server...');
    this.client.activate();
  }

  public disconnect(): void {
    if (!this.client || !this.client.active) {
      console.log('Not connected to STOMP server');
      return;
    }

    console.log('Disconnecting from STOMP server...');
    this.client.deactivate();
  }

  public isConnected(): boolean {
    return this.client !== null && this.client.active;
  }

  public subscribe(topic: string, callback: (data: any) => void): void {
    if (!this.client || !this.client.active) {
      console.log('Not connected to STOMP server, adding to pending subscriptions');
      this.pendingSubscriptions.push({ topic, callback });
      return;
    }

    console.log('Subscribing to topic:', topic);
    this.client.subscribe(topic, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (error) {
        console.error('Error parsing message body:', error);
        callback(message.body);
      }
    });
  }

  private processPendingSubscriptions(): void {
    if (!this.client || !this.client.active || this.pendingSubscriptions.length === 0) {
      return;
    }

    console.log('Processing pending subscriptions:', this.pendingSubscriptions.length);

    this.pendingSubscriptions.forEach(({ topic, callback }) => {
      this.subscribe(topic, callback);
    });

    this.pendingSubscriptions = [];
  }
}
