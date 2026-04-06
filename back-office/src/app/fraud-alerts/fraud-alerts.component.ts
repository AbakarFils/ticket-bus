import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { ApiService, FraudAlert } from '../core/api.service';

@Component({
  selector: 'app-fraud-alerts',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatSnackBarModule,
    MatSlideToggleModule, FormsModule, MatTooltipModule
  ],
  template: `
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
      <h2 style="margin:0">🚨 Alertes Fraude</h2>
      <div style="display:flex; gap:12px; align-items:center">
        <mat-slide-toggle [(ngModel)]="showOnlyUnresolved" (change)="load()">
          Non résolues uniquement
        </mat-slide-toggle>
        <button mat-raised-button color="primary" (click)="load()">Actualiser</button>
      </div>
    </div>

    <table mat-table [dataSource]="alerts" style="width:100%" *ngIf="alerts.length > 0">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let a">{{ a.id }}</td>
      </ng-container>
      <ng-container matColumnDef="ticketId">
        <th mat-header-cell *matHeaderCellDef>Ticket</th>
        <td mat-cell *matCellDef="let a">{{ a.ticketId }}</td>
      </ng-container>
      <ng-container matColumnDef="alertType">
        <th mat-header-cell *matHeaderCellDef>Type</th>
        <td mat-cell *matCellDef="let a">
          <span [style.background]="getTypeColor(a.alertType)"
                style="padding:4px 8px; border-radius:4px; font-size:0.85em; font-weight:500; color:white">
            {{ a.alertType }}
          </span>
        </td>
      </ng-container>
      <ng-container matColumnDef="description">
        <th mat-header-cell *matHeaderCellDef>Description</th>
        <td mat-cell *matCellDef="let a" style="max-width:300px">{{ a.description }}</td>
      </ng-container>
      <ng-container matColumnDef="location">
        <th mat-header-cell *matHeaderCellDef>Lieu</th>
        <td mat-cell *matCellDef="let a">{{ a.location }}</td>
      </ng-container>
      <ng-container matColumnDef="resolved">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let a" [style.color]="a.resolved ? 'green' : 'red'">
          {{ a.resolved ? '✅ Résolu' : '⚠️ Non résolu' }}
        </td>
      </ng-container>
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Date</th>
        <td mat-cell *matCellDef="let a">{{ a.createdAt | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let a">
          <button mat-icon-button color="primary"
                  (click)="resolve(a)"
                  [disabled]="a.resolved"
                  matTooltip="Marquer comme résolu">
            <mat-icon>check_circle</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="alerts.length === 0" style="color:#999; text-align:center; padding:16px">
      Aucune alerte fraude
    </p>
  `
})
export class FraudAlertsComponent implements OnInit {
  alerts: FraudAlert[] = [];
  showOnlyUnresolved = true;
  columns = ['id', 'ticketId', 'alertType', 'description', 'location', 'resolved', 'createdAt', 'actions'];

  constructor(private api: ApiService, private snack: MatSnackBar) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const obs = this.showOnlyUnresolved
      ? this.api.getUnresolvedFraudAlerts()
      : this.api.getFraudAlerts();
    obs.subscribe({
      next: (a) => this.alerts = a,
      error: () => this.snack.open('Erreur de chargement', 'OK', { duration: 3000 })
    });
  }

  resolve(alert: FraudAlert): void {
    this.api.resolveFraudAlert(alert.id).subscribe({
      next: (a) => {
        const idx = this.alerts.findIndex(x => x.id === a.id);
        if (idx >= 0) {
          if (this.showOnlyUnresolved) {
            this.alerts.splice(idx, 1);
            this.alerts = [...this.alerts];
          } else {
            this.alerts[idx] = a;
          }
        }
        this.snack.open(`Alerte #${a.id} résolue`, 'OK', { duration: 3000 });
      },
      error: () => this.snack.open('Erreur', 'OK', { duration: 3000 })
    });
  }

  getTypeColor(type: string): string {
    switch (type) {
      case 'TEMPORAL_COLLISION': return '#e53935';
      case 'BLACKLISTED': return '#6d4c41';
      case 'INVALID_SIGNATURE': return '#ff6f00';
      case 'DOUBLE_SCAN': return '#d84315';
      case 'SUSPICIOUS_PATTERN': return '#7b1fa2';
      default: return '#757575';
    }
  }
}

