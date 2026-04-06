import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ValidationRequest, ValidationResponse, ValidationEvent } from '../models/validation-event.model';
import { Page } from '../models/ticket.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class ValidationService {
  private http = inject(HttpClient);
  private api = inject(ApiService);
  validateTicket(req: ValidationRequest): Observable<ValidationResponse> { return this.http.post<ValidationResponse>(`${this.api.baseUrl}/validation/validate`, req); }
  getEvents(page = 0, size = 10): Observable<Page<ValidationEvent>> { return this.http.get<Page<ValidationEvent>>(`${this.api.baseUrl}/validation/events?page=${page}&size=${size}`); }
}
