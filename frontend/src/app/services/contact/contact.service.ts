import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable } from "rxjs"
import {ContactRequest} from '../../models/contact-request.model';

@Injectable({
  providedIn: "root",
})
export class ContactService {
  private apiUrl = "/api/contact"
  private agentApiUrl = "/api/agent/contact"

  constructor(private http: HttpClient) {}

  submitContactRequest(request: ContactRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/submit`, request)
  }

  getAllContactRequests(): Observable<ContactRequest[]> {
    return this.http.get<ContactRequest[]>(`${this.agentApiUrl}/all`)
  }

  getContactRequestById(id: number): Observable<ContactRequest> {
    return this.http.get<ContactRequest>(`${this.agentApiUrl}/get?requestId=${id}`)
  }

  updateContactRequestStatus(requestId: number, status: string, agentId: number): Observable<any> {
    return this.http.put(`${this.agentApiUrl}/update-status`, { requestId, status, agentId })
  }
}

