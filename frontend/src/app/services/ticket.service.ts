import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ticket, TicketRequest, Page } from '../models/ticket.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class TicketService {
  private http = inject(HttpClient);
  private api = inject(ApiService);
  createTicket(req: TicketRequest): Observable<Ticket> { return this.http.post<Ticket>(`${this.api.baseUrl}/tickets`, req); }
  getTickets(page = 0, size = 10): Observable<Page<Ticket>> { return this.http.get<Page<Ticket>>(`${this.api.baseUrl}/tickets?page=${page}&size=${size}`); }
  getTicket(id: string): Observable<Ticket> { return this.http.get<Ticket>(`${this.api.baseUrl}/tickets/${id}`); }
  cancelTicket(id: string): Observable<Ticket> { return this.http.put<Ticket>(`${this.api.baseUrl}/tickets/${id}/cancel`, {}); }
  getQrCode(id: string): Observable<Blob> { return this.http.get(`${this.api.baseUrl}/tickets/${id}/qrcode`, { responseType: 'blob' }); }
}
