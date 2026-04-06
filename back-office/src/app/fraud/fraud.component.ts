import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { ApiService, FraudAlert } from '../core/api.service';

@Component({
  selector: 'app-fraud',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatTableModule, MatButtonModule, MatCardModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatTabsModule
  ],
  template: `
    <h2>Gestion Fraude & Sécurité</h2>

    <mat-tab-group>
      <mat-tab label="Alertes actives ({{ activeAlerts.length }})">
        <div style="padding:16px">
          <div style="display:flex; justify-content:space-between; margin-bottom:12px">
            <span style="color:#666">Alertes non résolues</span>
            <button mat-raised-button color="primary" (click)="loadAlerts()">
              <mat-icon>refresh</mat-icon> Actualiser
            </button>
          </div>

          <table mat-table [dataSource]="activeAlerts" style="width:100%">
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
              <td mat-cell *matCellDef="let a" [style.color]="alertColor(a.alertType)">
                {{ a.alertType }}
              </td>
            </ng-container>
            <ng-container matColumnDef="description">
              <th mat-header-cell *matHeaderCellDef>Description</th>
              <td mat-cell *matCellDef="let a">{{ a.description }}</td>
            </ng-container>
            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let a">{{ a.createdAt | date:'short' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Actions</th>
              <td mat-cell *matCellDef="let a">
                <button mat-button color="primary" (click)="resolve(a)">Résoudre</button>
                <button mat-button color="warn" (click)="prefillBlacklist(a.ticketId)">Blacklister</button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="alertColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: alertColumns"></tr>
          </table>
          <p *ngIf="activeAlerts.length === 0" style="color:#999; text-align:center; padding:24px">
            Aucune alerte active
          </p>
        </div>
      </mat-tab>

      <mat-tab label="Blacklist ticket">
        <div style="padding:16px; max-width:520px">
          <h3>Blacklister un ticket</h3>
          <form [formGroup]="blacklistForm" (ngSubmit)="blacklist()"
                style="display:flex; flex-direction:column; gap:12px">
            <mat-form-field>
              <mat-label>ID du ticket</mat-label>
              <input matInput formControlName="ticketId" type="number">
            </mat-form-field>
            <mat-form-field>
              <mat-label>Raison</mat-label>
              <input matInput formControlName="reason" placeholder="Ex: Fraude détectée">
            </mat-form-field>
            <mat-form-field>
              <mat-label>Blacklisté par</mat-label>
              <input matInput formControlName="blacklistedBy" placeholder="Ex: admin">
            </mat-form-field>
            <button mat-raised-button color="warn" type="submit" [disabled]="blacklistForm.invalid">
              Blacklister
            </button>
          </form>
          <p *ngIf="blSuccess" style="color:green; margin-top:8px">{{ blSuccess }}</p>
          <p *ngIf="blError" style="color:red; margin-top:8px">{{ blError }}</p>
        </div>
      </mat-tab>
    </mat-tab-group>
  `
})
export class FraudComponent implements OnInit {
  activeAlerts: FraudAlert[] = [];
  alertColumns = ['id', 'ticketId', 'alertType', 'description', 'createdAt', 'actions'];
  blSuccess = '';
  blError = '';

  blacklistForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder) {
    this.blacklistForm = this.fb.group({
      ticketId: [null, [Validators.required, Validators.min(1)]],
      reason: ['', Validators.required],
      blacklistedBy: ['admin', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadAlerts();
  }

  loadAlerts(): void {
    this.api.getFraudAlerts().subscribe({
      next: (a) => this.activeAlerts = a,
      error: () => {}
    });
  }

  resolve(alert: FraudAlert): void {
    this.api.resolveFraudAlert(alert.id).subscribe({
      next: () => this.loadAlerts(),
      error: () => {}
    });
  }

  prefillBlacklist(ticketId: number): void {
    this.blacklistForm.patchValue({ ticketId });
  }

  blacklist(): void {
    this.blSuccess = '';
    this.blError = '';
    const { ticketId, reason, blacklistedBy } = this.blacklistForm.value;
    this.api.blacklistTicket(ticketId, reason, blacklistedBy).subscribe({
      next: () => {
        this.blSuccess = `Ticket ${ticketId} blacklisté.`;
        this.blacklistForm.reset({ blacklistedBy: 'admin' });
        this.loadAlerts();
      },
      error: (err: any) => {
        this.blError = 'Erreur : ' + (err?.error?.error || err.message);
      }
    });
  }

  alertColor(type: string): string {
    return type === 'DOUBLE_SCAN' ? 'orange' : 'red';
  }
}
