import { Injectable } from "@angular/core"
import { HttpClient, HttpParams } from "@angular/common/http"
import { Observable } from "rxjs"
import { AuditLog, AuditLogFilter } from "../../models/audit-log.model"
import { BACKEND_API_URL } from "../../../environments/api-config"

@Injectable({
  providedIn: "root",
})
export class AuditLogService {
  private apiUrl = BACKEND_API_URL + "/audit-logs"

  constructor(private http: HttpClient) {}

  getAllLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.apiUrl + "/all")
  }

  getLogsByUser(userId: number): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/user/${userId}`)
  }

  getLogsByAction(action: string): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(`${this.apiUrl}/action/${action}`)
  }

  getLogsByDateRange(startDate: Date, endDate: Date): Observable<AuditLog[]> {
    const params = new HttpParams().set("startDate", startDate.toISOString()).set("endDate", endDate.toISOString())

    return this.http.get<AuditLog[]>(`${this.apiUrl}/date-range`, { params })
  }

  getFilteredLogs(filter: AuditLogFilter): Observable<AuditLog[]> {
    let params = new HttpParams()

    if (filter.userId) {
      params = params.set("userId", filter.userId.toString())
    }

    if (filter.action) {
      params = params.set("action", filter.action)
    }

    if (filter.startDate) {
      params = params.set("startDate", filter.startDate.toISOString())
    }

    if (filter.endDate) {
      params = params.set("endDate", filter.endDate.toISOString())
    }

    return this.http.get<AuditLog[]>(`${this.apiUrl}/filter`, { params })
  }
}
