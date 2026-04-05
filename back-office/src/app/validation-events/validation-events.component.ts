import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { ApiService, ValidationEvent } from '../core/api.service';

@Component({
  selector: 'app-validation-events',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule],
  template: `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
      <h2 style="margin:0">Événements de validation</h2>
      <div style="display:flex;gap:8px;align-items:center">
        <span style="color:#666;font-size:0.9em">Actualisation auto toutes les 10s</span>
        <button mat-raised-button color="primary" (click)="load()">Actualiser</button>
      </div>
    </div>

    <table mat-table [dataSource]="events" style="width:100%">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let e">{{ e.id }}</td>
      </ng-container>
      <ng-container matColumnDef="ticketId">
        <th mat-header-cell *matHeaderCellDef>Ticket</th>
        <td mat-cell *matCellDef="let e">{{ e.ticketId }}</td>
      </ng-container>
      <ng-container matColumnDef="terminalId">
        <th mat-header-cell *matHeaderCellDef>Terminal</th>
        <td mat-cell *matCellDef="let e">{{ e.terminalId }}</td>
      </ng-container>
      <ng-container matColumnDef="location">
        <th mat-header-cell *matHeaderCellDef>Localisation</th>
        <td mat-cell *matCellDef="let e">{{ e.location }}</td>
      </ng-container>
      <ng-container matColumnDef="result">
        <th mat-header-cell *matHeaderCellDef>Résultat</th>
        <td mat-cell *matCellDef="let e" [style.color]="e.result === 'OK' ? 'green' : 'red'">{{ e.result }}</td>
      </ng-container>
      <ng-container matColumnDef="offline">
        <th mat-header-cell *matHeaderCellDef>Offline</th>
        <td mat-cell *matCellDef="let e">{{ e.offline ? '✓' : '' }}</td>
      </ng-container>
      <ng-container matColumnDef="timestamp">
        <th mat-header-cell *matHeaderCellDef>Horodatage</th>
        <td mat-cell *matCellDef="let e">{{ e.timestamp | date:'medium' }}</td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="events.length === 0" style="color:#999; text-align:center; padding:16px">
      Aucun événement disponible
    </p>
  `
})
export class ValidationEventsComponent implements OnInit, OnDestroy {
  events: ValidationEvent[] = [];
  columns = ['id', 'ticketId', 'terminalId', 'location', 'result', 'offline', 'timestamp'];
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.load();
    this.refreshInterval = setInterval(() => this.load(), 10000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval !== null) {
      clearInterval(this.refreshInterval);
    }
  }

  load(): void {
    this.api.getValidationEvents().subscribe({
      next: (evs) => this.events = evs,
      error: () => {}
    });
  }
}
