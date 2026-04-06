import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { OfflineService } from '../../services/offline.service';
import { ValidationService } from '../../services/validation.service';
import { TicketService } from '../../services/ticket.service';
import { ValidationEvent } from '../../models/validation-event.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, AsyncPipe],
  template: `
    <h1 class="page-title">Tableau de bord</h1>
    <div class="grid-4" style="margin-bottom:1.5rem">
      <div class="card">
        <div style="font-size:2rem;font-weight:700">{{ totalTickets }}</div>
        <div style="color:#666">Tickets créés</div>
      </div>
      <div class="card">
        <div style="font-size:2rem;font-weight:700">{{ totalEvents }}</div>
        <div style="color:#666">Validations</div>
      </div>
      <div class="card">
        <div style="font-size:2rem;font-weight:700">{{ pendingCount }}</div>
        <div style="color:#666">En attente sync</div>
      </div>
      <div class="card">
        <span class="status-dot" [class.online]="(offline.isOnline | async)" [class.offline]="!(offline.isOnline | async)"></span>
        <span>{{ (offline.isOnline | async) ? 'En ligne' : 'Hors ligne' }}</span>
      </div>
    </div>
    <div class="grid-2">
      <div class="card">
        <h2 class="section-title">Actions rapides</h2>
        <div style="display:flex;flex-direction:column;gap:.5rem">
          <a routerLink="/tickets/create" class="btn btn-primary">+ Créer un ticket</a>
          <a routerLink="/validation" class="btn btn-success">🔍 Valider un QR</a>
          <a routerLink="/sync" class="btn btn-secondary">↻ Synchronisation</a>
        </div>
      </div>
      <div class="card">
        <h2 class="section-title">Validations récentes</h2>
        <table>
          <tr><th>Ticket</th><th>Statut</th><th>Lieu</th></tr>
          @for (e of recentEvents; track e.id) {
            <tr>
              <td>{{ e.validatorDeviceId }}</td>
              <td><span class="badge badge-{{ e.status }}">{{ e.status }}</span></td>
              <td>{{ e.location }}</td>
            </tr>
          }
          @if (!recentEvents.length) { <tr><td colspan="3" style="text-align:center;color:#999">Aucune validation</td></tr> }
        </table>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  offline = inject(OfflineService);
  private validationSvc = inject(ValidationService);
  private ticketSvc = inject(TicketService);
  recentEvents: ValidationEvent[] = [];
  totalTickets = 0; totalEvents = 0; pendingCount = 0;
  ngOnInit() {
    this.pendingCount = this.offline.getPendingCount();
    this.validationSvc.getEvents(0, 5).subscribe({ next: p => { this.recentEvents = p.content; this.totalEvents = p.totalElements; }, error: () => {} });
    this.ticketSvc.getTickets(0, 1).subscribe({ next: p => this.totalTickets = p.totalElements, error: () => {} });
  }
}
