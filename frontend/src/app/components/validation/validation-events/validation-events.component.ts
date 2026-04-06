import { Component, inject, OnInit } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { ValidationService } from '../../../services/validation.service';
import { ValidationEvent } from '../../../models/validation-event.model';

@Component({
  selector: 'app-validation-events',
  standalone: true,
  imports: [SlicePipe],
  template: `
    <h1 class="page-title">Historique des validations</h1>
    <div class="card">
      <table>
        <tr><th>Appareil</th><th>Heure</th><th>Lieu</th><th>Statut</th><th>Synchronisé</th></tr>
        @for (e of events; track e.id) {
          <tr>
            <td>{{ e.validatorDeviceId }}</td>
            <td>{{ e.validationTime | slice:0:16 }}</td>
            <td>{{ e.location }}</td>
            <td><span class="badge badge-{{ e.status }}">{{ e.status }}</span></td>
            <td>{{ e.synced ? '✅' : '⏳' }}</td>
          </tr>
        }
        @if (!events.length) { <tr><td colspan="5" style="text-align:center;color:#999">Aucun événement</td></tr> }
      </table>
      <div class="pagination">
        <button class="btn btn-sm btn-secondary" [disabled]="page === 0" (click)="changePage(page-1)">‹ Préc.</button>
        <span>Page {{ page+1 }} / {{ totalPages }}</span>
        <button class="btn btn-sm btn-secondary" [disabled]="page >= totalPages-1" (click)="changePage(page+1)">Suiv. ›</button>
      </div>
    </div>
  `
})
export class ValidationEventsComponent implements OnInit {
  private svc = inject(ValidationService);
  events: ValidationEvent[] = []; page = 0; totalPages = 1;
  ngOnInit() { this.load(); }
  load() { this.svc.getEvents(this.page, 10).subscribe({ next: p => { this.events = p.content; this.totalPages = p.totalPages || 1; }, error: () => {} }); }
  changePage(p: number) { this.page = p; this.load(); }
}
