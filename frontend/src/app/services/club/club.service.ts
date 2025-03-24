import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PROXY_API_URL} from '../../../environments/api-config';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ClubService {
  private apiUrl = PROXY_API_URL + '/clubs';

  constructor(private http : HttpClient) {}

  getClubs() {
    return this.http.get<any[]>(`${this.apiUrl}/list`);
  }

  getClub(id: number) {
    return this.http.get<any>(`${this.apiUrl}/get`, {
      params: {id: id},
    });
  }

  createClub(name: string, description: string, isPublic : boolean, userId : number) :Observable<any> {
    return this.http.post<any[]>(`${this.apiUrl}/create`, null, {
      params: {name: name, description: description,  isPublic: isPublic, userId: userId},
    });
  }

  updateClub(id: number, name: string, description: string, isPublic : boolean) {
    return this.http.put<any[]>(`${this.apiUrl}/update`, null, {
      params: {id: id, name: name, description: description},
    });
  }

  deleteClub(id: number) {
    return this.http.delete<any[]>(`${this.apiUrl}/delete`, {
      params: {id: id},
    });
  }

}
