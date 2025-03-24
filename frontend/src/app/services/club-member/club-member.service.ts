import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PROXY_API_URL} from '../../../environments/api-config';

@Injectable({
  providedIn: 'root'
})
export class ClubMemberService {
  private apiUrl = PROXY_API_URL + '/clubmembers';

  constructor(private http: HttpClient) { }

}
