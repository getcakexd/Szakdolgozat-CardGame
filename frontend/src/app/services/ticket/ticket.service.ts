import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Ticket, TicketMessage } from '../../models/ticket.model';
import { BACKEND_API_URL } from '../../../environments/api-config';

@Injectable({
  providedIn: "root",
})
export class TicketService {
  private apiUrl = BACKEND_API_URL + "/tickets";
  private agentApiUrl = BACKEND_API_URL + "/agent/tickets";

  constructor(private http: HttpClient) {}

  createTicket(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.apiUrl}/create`, ticket);
  }

  getTicketById(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/get/${id}`);
  }

  getTicketByReference(reference: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.apiUrl}/reference/${reference}`);
  }

  getUserTickets(userId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.apiUrl}/user/${userId}`);
  }

  getAllTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.agentApiUrl}/all`);
  }

  updateTicketStatus(ticketId: string, status: 'new' | 'in-progress' | 'resolved', agentId: number): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.agentApiUrl}/update-status`, { ticketId, status, agentId });
  }

  getTicketMessages(ticketId: string): Observable<TicketMessage[]> {
    return this.http.get<TicketMessage[]>(`${this.apiUrl}/messages/${ticketId}`);
  }

  addTicketMessage(message: TicketMessage): Observable<TicketMessage> {
    let params = new HttpParams()
      .set('ticketId', message.ticketId.toString())
      .set('message', message.message)
      .set('senderName', message.senderName);

    if (message.senderEmail) {
      params = params.set('senderEmail', message.senderEmail);
    }

    if (message.senderType) {
      params = params.set('senderType', message.senderType);
    }

    if (message.userId) {
      params = params.set('userId', message.userId.toString());
    }

    return this.http.post<TicketMessage>(`${this.apiUrl}/messages/add`, null, { params });
  }

  generateTicketReference(): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < 8; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }
}
