import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ApiService, PaymentTransaction } from '../core/api.service';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatTableModule, MatIconModule,
    MatSnackBarModule, MatTooltipModule
  ],
  template: `
    <h2>Historique des Paiements</h2>

    <mat-card style="max-width:480px; margin-bottom:24px">
      <mat-card-header><mat-card-title>Rechercher par utilisateur</mat-card-title></mat-card-header>
      <mat-card-content>
        <form [formGroup]="searchForm" (ngSubmit)="onSearch()" style="display:flex;gap:12px;align-items:flex-end">
          <mat-form-field>
            <mat-label>User ID</mat-label>
            <input matInput formControlName="userId" type="number" placeholder="Ex: 1">
          </mat-form-field>
          <button mat-raised-button color="primary" type="submit" [disabled]="searchForm.invalid">Rechercher</button>
          <button mat-raised-button type="button" (click)="loadRecent()">Tous récents</button>
        </form>
      </mat-card-content>
    </mat-card>

    <table mat-table [dataSource]="transactions" style="width:100%" *ngIf="transactions.length > 0">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef>ID</th>
        <td mat-cell *matCellDef="let t">{{ t.id }}</td>
      </ng-container>
      <ng-container matColumnDef="userId">
        <th mat-header-cell *matHeaderCellDef>Utilisateur</th>
        <td mat-cell *matCellDef="let t">{{ t.userId }}</td>
      </ng-container>
      <ng-container matColumnDef="amount">
        <th mat-header-cell *matHeaderCellDef>Montant</th>
        <td mat-cell *matCellDef="let t">{{ t.amount | number }} {{ t.currency }}</td>
      </ng-container>
      <ng-container matColumnDef="type">
        <th mat-header-cell *matHeaderCellDef>Type</th>
        <td mat-cell *matCellDef="let t"
            [style.color]="t.type === 'TOPUP' ? 'green' : t.type === 'REFUND' ? 'blue' : 'black'">
          {{ t.type }}
        </td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Statut</th>
        <td mat-cell *matCellDef="let t"
            [style.color]="t.status === 'SUCCESS' ? 'green' : t.status === 'FAILED' ? 'red' : 'orange'">
          {{ t.status }}
        </td>
      </ng-container>
      <ng-container matColumnDef="paymentMethod">
        <th mat-header-cell *matHeaderCellDef>Méthode</th>
        <td mat-cell *matCellDef="let t">{{ t.paymentMethod }}</td>
      </ng-container>
      <ng-container matColumnDef="transactionRef">
        <th mat-header-cell *matHeaderCellDef>Référence</th>
        <td mat-cell *matCellDef="let t" style="font-size:0.85em">{{ t.transactionRef }}</td>
      </ng-container>
      <ng-container matColumnDef="createdAt">
        <th mat-header-cell *matHeaderCellDef>Date</th>
        <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'medium' }}</td>
      </ng-container>
      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let t">
          <button mat-icon-button color="warn"
                  (click)="refund(t)"
                  [disabled]="t.status !== 'SUCCESS' || t.type === 'REFUND'"
                  matTooltip="Rembourser">
            <mat-icon>undo</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="columns"></tr>
      <tr mat-row *matRowDef="let row; columns: columns"></tr>
    </table>

    <p *ngIf="transactions.length === 0" style="color:#999; text-align:center; padding:16px">
      Aucune transaction trouvée
    </p>
  `
})
export class PaymentsComponent implements OnInit {
  transactions: PaymentTransaction[] = [];
  columns = ['id', 'userId', 'amount', 'type', 'status', 'paymentMethod', 'transactionRef', 'createdAt', 'actions'];

  searchForm: FormGroup;

  constructor(private api: ApiService, private fb: FormBuilder, private snack: MatSnackBar) {
    this.searchForm = this.fb.group({
      userId: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadRecent();
  }

  loadRecent(): void {
    this.api.getRecentPayments().subscribe({
      next: (t) => this.transactions = t,
      error: () => this.snack.open('Erreur de chargement', 'OK', { duration: 3000 })
    });
  }

  onSearch(): void {
    const userId = this.searchForm.value['userId'] as number;
    this.api.getPaymentsByUser(userId).subscribe({
      next: (t) => this.transactions = t,
      error: () => this.snack.open('Erreur de recherche', 'OK', { duration: 3000 })
    });
  }

  refund(tx: PaymentTransaction): void {
    this.api.refundPayment(tx.transactionRef).subscribe({
      next: () => {
        this.snack.open('Remboursement effectué', 'OK', { duration: 3000 });
        this.loadRecent();
      },
      error: () => this.snack.open('Erreur lors du remboursement', 'OK', { duration: 3000 })
    });
  }
}

