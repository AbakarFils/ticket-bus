import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../../services/ticket.service';
import { Ticket } from '../../../models/ticket.model';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1.5rem">
      <h1 class="page-title" style="margin:0">Tickets</h1>
      <a routerLink="/tickets/create" class="btn btn-primary">+ Nouveau ticket</a>
    </div>
    <div class="card">
      <table>
        <tr><th>N° Ticket</th><th>Passager</th><th>Ligne</th><th>Statut</th><th>Actions</th></tr>
        @for (t of tickets; track t.id) {
          <tr>
            <td>{{ t.ticketNumber }}</td>
            <td>{{ t.passengerName }}</td>
            <td>{{ t.routeName }}</td>
            <td><span class="badge badge-{{ t.status }}">{{ t.status }}</span></td>
            <td>
              <a routerLink="/tickets/{{ t.id }}" class="btn btn-sm btn-secondary">Voir</a>
            </td>
          </tr>
        }
        @if (!tickets.length) { <tr><td colspan="5" style="text-align:center;color:#999">Aucun ticket</td></tr> }
      </table>
      <div class="pagination">
        <button class="btn btn-sm btn-secondary" [disabled]="page === 0" (click)="changePage(page-1)">‹ Préc.</button>
        <span>Page {{ page+1 }} / {{ totalPages }}</span>
        <button class="btn btn-sm btn-secondary" [disabled]="page >= totalPages-1" (click)="changePage(page+1)">Suiv. ›</button>
      </div>
    </div>
  `
})
export class TicketListComponent implements OnInit {
  private svc = inject(TicketService);
  tickets: Ticket[] = []; page = 0; totalPages = 1;
  ngOnInit() { this.load(); }
  load() { this.svc.getTickets(this.page, 10).subscribe({ next: p => { this.tickets = p.content; this.totalPages = p.totalPages || 1; }, error: () => {} }); }
  changePage(p: number) { this.page = p; this.load(); }
}
