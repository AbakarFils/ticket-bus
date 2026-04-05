import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { ApiService, Wallet } from '../core/api.service';

@Component({
  selector: 'app-wallets',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule
  ],
  template: `
    <h2>Gestion des Wallets</h2>

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

    <mat-card *ngIf="wallet" style="max-width:480px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Wallet #{{ wallet.id }}</mat-card-title></mat-card-header>
      <mat-card-content>
        <p><strong>Utilisateur :</strong> {{ wallet.userId }}</p>
        <p><strong>Solde :</strong> {{ wallet.balance | number }} {{ wallet.currency }}</p>

        <h4>Recharger</h4>
        <form [formGroup]="topUpForm" (ngSubmit)="onTopUp()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>Montant ({{ wallet.currency }})</mat-label>
            <input matInput formControlName="amount" type="number" placeholder="Ex: 5000">
          </mat-form-field>
          <button mat-raised-button color="accent" type="submit" [disabled]="topUpForm.invalid">Recharger</button>
        </form>
        <p *ngIf="topUpSuccess" style="color:green">{{ topUpSuccess }}</p>
        <p *ngIf="topUpError" style="color:red">{{ topUpError }}</p>
      </mat-card-content>
    </mat-card>

    <p *ngIf="searchError" style="color:red">{{ searchError }}</p>
  `
})
export class WalletsComponent {
  wallet: Wallet | null = null;
  searchError = '';
  topUpSuccess = '';
  topUpError = '';

  searchForm: FormGroup;
  topUpForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]]
    });
    this.topUpForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(1)]]
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
    this.topUpSuccess = '';
    this.topUpError = '';
    const amount = this.topUpForm.value['amount'] as number;
    this.api.topUpWallet(this.wallet.userId, amount).subscribe({
      next: (w) => {
        this.wallet = w;
        this.topUpSuccess = `Rechargement de ${amount} ${w.currency} effectué. Nouveau solde : ${w.balance} ${w.currency}`;
        this.topUpForm.reset();
      },
      error: () => { this.topUpError = 'Erreur lors du rechargement.'; }
    });
  }
}
