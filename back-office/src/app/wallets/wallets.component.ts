import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ApiService, Wallet } from '../core/api.service';

@Component({
  selector: 'app-wallets',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatSnackBarModule, MatIconModule, MatProgressBarModule
  ],
  template: `
    <h2>Gestion des Wallets</h2>

    <!-- Budget alerts section -->
    <mat-card *ngIf="alertWallets.length > 0" style="margin-bottom:24px;border-left:4px solid #c62828">
      <mat-card-header>
        <mat-icon matCardAvatar style="color:#c62828;font-size:28px;width:36px;height:36px">warning</mat-icon>
        <mat-card-title>⚠️ Wallets en alerte budget ({{ alertWallets.length }})</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div *ngFor="let aw of alertWallets" style="display:flex;gap:16px;align-items:center;padding:8px 0;border-bottom:1px solid #eee">
          <strong>User #{{ aw.userId }}</strong>
          <span>{{ aw.monthlySpent | number }} / {{ aw.monthlyBudget | number }} {{ aw.currency }}</span>
          <mat-progress-bar style="flex:1;max-width:200px" mode="determinate"
            [value]="aw.monthlyBudget ? ((aw.monthlySpent ?? 0) / aw.monthlyBudget * 100) : 0"
            [color]="aw.budgetAlertTriggered ? 'warn' : 'primary'">
          </mat-progress-bar>
          <span style="color:#c62828;font-weight:500">{{ aw.monthlyBudget ? ((aw.monthlySpent ?? 0) / aw.monthlyBudget * 100 | number:'1.0-0') : 0 }}%</span>
        </div>
      </mat-card-content>
    </mat-card>

    <mat-card style="max-width:480px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Consulter un wallet</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="searchForm" (ngSubmit)="onSearch()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>User ID</mat-label>
            <input matInput formControlName="userId" type="number" placeholder="Ex: 1">
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="searchForm.invalid">Rechercher</button>
        </form>
      </mat-card-content>
    </mat-card>

    <mat-card *ngIf="wallet" style="max-width:650px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Wallet #{{ wallet.id }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <div style="display:flex; gap:24px; flex-wrap:wrap; margin-bottom:16px">
          <div>
            <p style="margin:0;color:#666;font-size:0.85em">Utilisateur</p>
            <p style="margin:0;font-size:1.2em;font-weight:500">{{ wallet.userId }}</p>
          </div>
          <div>
            <p style="margin:0;color:#666;font-size:0.85em">Solde</p>
            <p style="margin:0;font-size:1.2em;font-weight:500;color:#2e7d32">{{ wallet.balance | number }} {{ wallet.currency }}</p>
          </div>
          <div *ngIf="wallet.monthlyBudget">
            <p style="margin:0;color:#666;font-size:0.85em">Budget mensuel</p>
            <p style="margin:0;font-size:1.2em;font-weight:500">{{ wallet.monthlyBudget | number }} {{ wallet.currency }}</p>
          </div>
          <div *ngIf="wallet.monthlySpent != null">
            <p style="margin:0;color:#666;font-size:0.85em">Dépensé ce mois</p>
            <p style="margin:0;font-size:1.2em;font-weight:500"
               [style.color]="wallet.budgetAlertTriggered ? 'red' : 'black'">
              {{ wallet.monthlySpent | number }} {{ wallet.currency }}
              <span *ngIf="wallet.budgetAlertTriggered" style="font-size:0.8em"> ⚠️ Seuil atteint</span>
            </p>
          </div>
        </div>

        <!-- Budget progress bar -->
        <div *ngIf="wallet.monthlyBudget" style="margin-bottom:16px">
          <p style="margin:0 0 4px;color:#666;font-size:0.85em">Consommation budget:
            {{ wallet.monthlyBudget ? ((wallet.monthlySpent ?? 0) / wallet.monthlyBudget * 100 | number:'1.0-0') : 0 }}%
          </p>
          <mat-progress-bar mode="determinate"
            [value]="wallet.monthlyBudget ? ((wallet.monthlySpent ?? 0) / wallet.monthlyBudget * 100) : 0"
            [color]="wallet.budgetAlertTriggered ? 'warn' : 'primary'">
          </mat-progress-bar>
        </div>

        <h4>Recharger</h4>
        <form [formGroup]="topUpForm" (ngSubmit)="onTopUp()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>Montant ({{ wallet.currency }})</mat-label>
            <input matInput formControlName="amount" type="number" placeholder="Ex: 5000">
          </mat-form-field>
          <button mat-raised-button color="accent" type="submit" [disabled]="topUpForm.invalid">Recharger</button>
        </form>

        <h4>Configurer budget mensuel</h4>
        <form [formGroup]="budgetForm" (ngSubmit)="onSetBudget()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>Budget ({{ wallet.currency }})</mat-label>
            <input matInput formControlName="monthlyBudget" type="number" placeholder="Ex: 50000">
          </mat-form-field>
          <mat-form-field>
            <mat-label>Seuil alerte (%)</mat-label>
            <input matInput formControlName="alertThresholdPercent" type="number" placeholder="Ex: 80">
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="budgetForm.invalid">Configurer</button>
        </form>
      </mat-card-content>
    </mat-card>

    <p *ngIf="searchError" style="color:red">{{ searchError }}</p>
  `
})
export class WalletsComponent implements OnInit {
  wallet: Wallet | null = null;
  alertWallets: Wallet[] = [];
  searchError = '';

  searchForm: FormGroup;
  topUpForm: FormGroup;
  budgetForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.searchForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]]
    });
    this.topUpForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(1)]]
    });
    this.budgetForm = this.fb.group({
      monthlyBudget: [null, [Validators.required, Validators.min(0)]],
      alertThresholdPercent: [80, [Validators.required, Validators.min(1), Validators.max(100)]]
    });
  }

  ngOnInit(): void {
    this.loadAlerts();
  }

  loadAlerts(): void {
    this.api.getWalletAlerts().subscribe({
      next: (w) => this.alertWallets = w,
      error: () => {}
    });
  }

  onSearch(): void {
    this.searchError = '';
    this.wallet = null;
    const userId = this.searchForm.value['userId'] as number;
    this.api.getWallet(userId).subscribe({
      next: (w) => this.wallet = w,
      error: () => { this.searchError = `Wallet introuvable pour l'utilisateur ${userId}.`; }
    });
  }

  onTopUp(): void {
    if (!this.wallet || this.topUpForm.invalid) return;
    const amount = this.topUpForm.value['amount'] as number;
    this.api.topUpWallet(this.wallet.userId, amount).subscribe({
      next: (w) => {
        this.wallet = w;
        this.snack.open(`Rechargement de ${amount} ${w.currency} effectué. Nouveau solde : ${w.balance} ${w.currency}`, 'OK', { duration: 3000 });
        this.topUpForm.reset();
      },
      error: () => this.snack.open('Erreur lors du rechargement.', 'OK', { duration: 3000 })
    });
  }

  onSetBudget(): void {
    if (!this.wallet || this.budgetForm.invalid) return;
    const budget = this.budgetForm.value['monthlyBudget'] as number;
    const threshold = this.budgetForm.value['alertThresholdPercent'] as number;
    this.api.setMonthlyBudget(this.wallet.userId, budget, threshold).subscribe({
      next: (w) => {
        this.wallet = w;
        this.snack.open(`Budget mensuel configuré : ${budget} ${w.currency}`, 'OK', { duration: 3000 });
        this.loadAlerts();
      },
      error: () => this.snack.open('Erreur de configuration du budget.', 'OK', { duration: 3000 })
    });
  }
}
