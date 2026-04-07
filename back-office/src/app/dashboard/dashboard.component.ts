import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { ApiService, ValidationEvent, TicketStats, Wallet } from '../core/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressBarModule, RouterLink],
  template: `
    <h2>Dashboard</h2>
    <div style="display:flex; gap:16px; flex-wrap:wrap; margin-bottom:24px">
      <mat-card style="min-width:200px; cursor:pointer" routerLink="/events">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:32px;width:40px;height:40px;color:#1565c0">verified</mat-icon>
          <mat-card-title>Total Validations</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1>{{ totalValidations }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:200px; cursor:pointer" routerLink="/events">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:32px;width:40px;height:40px;color:#2e7d32">check_circle</mat-icon>
          <mat-card-title>Validations OK</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1 style="color:green">{{ successValidations }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:200px; cursor:pointer" routerLink="/fraud-alerts">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:32px;width:40px;height:40px;color:#c62828">warning</mat-icon>
          <mat-card-title>Alertes fraude</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1 style="color:red">{{ fraudAlerts }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:200px">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:32px;width:40px;height:40px;color:#f57c00">cloud_off</mat-icon>
          <mat-card-title>Événements offline</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1>{{ offlineEvents }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:200px; cursor:pointer" routerLink="/tickets">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:32px;width:40px;height:40px;color:#6a1b9a">confirmation_number</mat-icon>
          <mat-card-title>Tickets actifs</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1>{{ ticketStats.totalActive }}</h1></mat-card-content>
      </mat-card>
    </div>

    <!-- Pass / Carnet / Budget KPIs -->
    <div style="display:flex; gap:16px; flex-wrap:wrap; margin-bottom:24px">
      <mat-card style="min-width:180px;border-left:4px solid #2e7d32">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:28px;width:36px;height:36px;color:#2e7d32">card_membership</mat-icon>
          <mat-card-title>Pass actifs</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1 style="color:#2e7d32">{{ ticketStats.passActive }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:180px;border-left:4px solid #f57c00">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:28px;width:36px;height:36px;color:#f57c00">style</mat-icon>
          <mat-card-title>Carnets actifs</mat-card-title>
        </mat-card-header>
        <mat-card-content><h1 style="color:#f57c00">{{ ticketStats.carnetActive }}</h1></mat-card-content>
      </mat-card>
      <mat-card style="min-width:220px;border-left:4px solid #f57c00">
        <mat-card-header>
          <mat-card-title>Taux utilisation carnets</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <h1>{{ ticketStats.carnetUsageRatePercent }}%</h1>
          <mat-progress-bar mode="determinate" [value]="ticketStats.carnetUsageRatePercent" color="warn"></mat-progress-bar>
        </mat-card-content>
      </mat-card>
      <mat-card style="min-width:200px;border-left:4px solid #c62828" *ngIf="budgetAlerts.length > 0">
        <mat-card-header>
          <mat-icon matCardAvatar style="font-size:28px;width:36px;height:36px;color:#c62828">account_balance_wallet</mat-icon>
          <mat-card-title>Alertes budget</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <h1 style="color:#c62828">{{ budgetAlerts.length }}</h1>
          <p style="font-size:0.85em;color:#666">wallets en dépassement</p>
        </mat-card-content>
      </mat-card>
    </div>

    <h3>Événements récents</h3>
    <table style="width:100%; border-collapse:collapse">
      <thead>
        <tr style="background:#f5f5f5">
          <th style="text-align:left;padding:8px">ID</th>
          <th style="text-align:left;padding:8px">Ticket</th>
          <th style="text-align:left;padding:8px">Terminal</th>
          <th style="text-align:left;padding:8px">Localisation</th>
          <th style="text-align:left;padding:8px">Résultat</th>
          <th style="text-align:left;padding:8px">Offline</th>
          <th style="text-align:left;padding:8px">Horodatage</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let ev of recentEvents" style="border-bottom:1px solid #eee">
          <td style="padding:8px">{{ ev.id }}</td>
          <td style="padding:8px">{{ ev.ticketId }}</td>
          <td style="padding:8px">{{ ev.terminalId }}</td>
          <td style="padding:8px">{{ ev.location }}</td>
          <td style="padding:8px" [style.color]="ev.result === 'OK' ? 'green' : 'red'">{{ ev.result }}</td>
          <td style="padding:8px">{{ ev.offline ? 'Oui' : 'Non' }}</td>
          <td style="padding:8px">{{ ev.timestamp | date:'short' }}</td>
        </tr>
        <tr *ngIf="recentEvents.length === 0">
          <td colspan="7" style="padding:16px; text-align:center; color:#999">Aucun événement</td>
        </tr>
      </tbody>
    </table>
  `
})
export class DashboardComponent implements OnInit {
  recentEvents: ValidationEvent[] = [];
  totalValidations = 0;
  successValidations = 0;
  fraudAlerts = 0;
  offlineEvents = 0;
  ticketStats: TicketStats = { totalActive: 0, passActive: 0, carnetActive: 0, carnetUsageRatePercent: 0 };
  budgetAlerts: Wallet[] = [];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getValidationEvents().subscribe({
      next: (evs) => {
        this.totalValidations = evs.length;
        this.successValidations = evs.filter(e => e.result === 'OK').length;
        this.fraudAlerts = evs.filter(e => e.result !== 'OK').length;
        this.offlineEvents = evs.filter(e => e.offline).length;
        this.recentEvents = evs.slice(0, 20);
      },
      error: () => {}
    });

    this.api.getTicketStats().subscribe({
      next: (s) => this.ticketStats = s,
      error: () => {}
    });

    this.api.getWalletAlerts().subscribe({
      next: (w) => this.budgetAlerts = w,
      error: () => {}
    });
  }
}
